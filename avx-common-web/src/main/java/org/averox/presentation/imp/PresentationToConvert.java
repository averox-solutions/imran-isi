package org.averox.presentation.imp;

import org.averox.presentation.UploadedPresentation;

public class PresentationToConvert {
    public final UploadedPresentation pres;
    private int pagesCompleted = 0;

    public PresentationToConvert(UploadedPresentation pres) {
        this.pres = pres;
    }

    public String getKey() {
        return pres.getId();
    }

    public int getPagesCompleted() {
        return pagesCompleted;
    }

    public void incrementPagesCompleted() {
        pagesCompleted++;
    }
}
