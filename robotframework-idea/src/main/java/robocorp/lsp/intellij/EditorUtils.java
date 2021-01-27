/*
 * Original work Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) (Apache 2.0)
 * See ThirdPartyNotices.txt in the project root for license information.
 * All modifications Copyright (c) Robocorp Technologies Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http: // www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package robocorp.lsp.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.BadLocationException;
import java.util.*;

/**
 * Various methods to convert offsets / logical position / server position
 */
public class EditorUtils {

    private static Logger LOG = Logger.getInstance(EditorUtils.class);
    public static final String WIN_SEPARATOR = "\r\n";
    public static final String LINUX_SEPARATOR = "\n";

    private static final Comparator<TextEdit> TEXT_EDIT_COMPARATOR = new Comparator<TextEdit>() {

        @Override
        public int compare(TextEdit t1, TextEdit t2) {
            return compare(t1.getRange(), t2.getRange());
        }

        public int compare(Range r1, Range r2) {
            int ret = compare(r1.getStart(), r2.getStart());
            if (ret != 0) {
                return ret;
            }
            return compare(r1.getEnd(), r2.getEnd());
        }

        public int compare(Position p1, Position p2) {
            int ret = p1.getLine() - p2.getLine();
            if (ret != 0) {
                return ret;
            }
            return p1.getCharacter() - p2.getCharacter();
        }
    };

    /**
     * Gets the line at the given offset given an editor and bolds the text between the given offsets
     *
     * @param editor      The editor
     * @param startOffset The starting offset
     * @param endOffset   The ending offset
     * @return The document line
     */
    public static String getLineText(Editor editor, int startOffset, int endOffset) {
        return runReadAction(() -> {
            Document doc = editor.getDocument();
            int lineIdx = doc.getLineNumber(startOffset);
            int lineStartOff = doc.getLineStartOffset(lineIdx);
            int lineEndOff = doc.getLineEndOffset(lineIdx);
            String line = doc.getText(new TextRange(lineStartOff, lineEndOff));
            int startOffsetInLine = startOffset - lineStartOff;
            int endOffsetInLine = endOffset - lineStartOff;
            return runReadAction(() -> line.substring(0, startOffsetInLine) + "<b>" + line
                    .substring(startOffsetInLine, endOffsetInLine) + "</b>" + line.substring(endOffsetInLine));
        });
    }

    /**
     * Calculates a Position given an editor and an offset
     *
     * @param editor The editor
     * @param offset The offset
     * @return an LSP position
     */
    public static Position offsetToLSPPos(Editor editor, int offset) {
        return runReadAction(() -> {
            return offsetToLSPPos(editor.getDocument(), offset);
        });
    }

    public static Position offsetToLSPPos(Document doc, final int offset) {
        return runReadAction(() -> {
            int newOffset = offset;
            int line = doc.getLineNumber(newOffset);
            int lineStartOffset = doc.getLineStartOffset(line);
            int lineEndOffset = doc.getLineEndOffset(line);
            if (newOffset > lineEndOffset) {
                newOffset = lineEndOffset;
            }
            int column = newOffset - lineStartOffset;
            return new Position(line, column);
        });
    }

    public static int LSPPosToOffset(Editor editor, Position pos) {
        if (editor.isDisposed()) {
            return -1;
        }
        return LSPPosToOffset(editor.getDocument(), pos);
    }

    /**
     * Transforms an LSP position to an editor offset
     *
     * @param pos The LSPPos
     * @return The offset
     */
    public static int LSPPosToOffset(Document doc, Position pos) {
        return runReadAction(() -> {
            try {
                int line = Math.max(0, Math.min(pos.getLine(), doc.getLineCount()));
                int lineStartOffset = doc.getLineStartOffset(line);
                int lineEndOffset = doc.getLineEndOffset(line);

                return Math.min(lineStartOffset + pos.getCharacter(), lineEndOffset);
            } catch (IndexOutOfBoundsException e) {
                return -1;
            }
        });
    }

    public static @Nullable VirtualFile getVirtualFile(Editor editor) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        return file;
    }

    public static @Nullable LanguageServerDefinition getLanguageDefinition(VirtualFile file) {
        FileType fileType = file.getFileType();
        if (fileType instanceof LanguageFileType) {
            Language language = ((LanguageFileType) fileType).getLanguage();
            if (language instanceof ILSPLanguage) {
                LanguageServerDefinition definition = ((ILSPLanguage) language).getLanguageDefinition();
                return definition;
            }
        }
        return null;
    }

    static public void runWriteAction(Runnable runnable) {
        ApplicationManager.getApplication().runWriteAction(runnable);
    }

    static public <T> T runReadAction(Computable<T> computable) {
        return ApplicationManager.getApplication().runReadAction(computable);
    }

    public static void applyTextEdits(Document doc, Collection<? extends TextEdit> edits) throws BadLocationException {
        List<TextEdit> sortedEdits = new ArrayList<>(edits);
        Collections.sort(sortedEdits, TEXT_EDIT_COMPARATOR);
        runWriteAction(() -> {
            for (int i = sortedEdits.size() - 1; i >= 0; i--) {
                // Do it backwards so that we edit from the end of the file to the start (otherwise
                // offsets become invalid as we edit).
                TextEdit te = sortedEdits.get(i);
                Range r = te.getRange();
                if (r != null && r.getStart() != null && r.getEnd() != null) {
                    int start = LSPPosToOffset(doc, r.getStart());
                    int end = LSPPosToOffset(doc, r.getEnd());
                    doc.replaceString(start, end, te.getNewText());
                }
            }
        });
    }

}
