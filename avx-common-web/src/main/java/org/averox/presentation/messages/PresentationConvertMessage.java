package org.averox.presentation.messages;

import org.averox.presentation.UploadedPresentation;

public class PresentationConvertMessage implements IPresentationCompletionMessage {
    public final UploadedPresentation pres;

    public PresentationConvertMessage(UploadedPresentation pres) {
        this.pres = pres;
    }
}
