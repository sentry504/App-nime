package com.sentry504.scraperprueba.libraryepisodes;

public class LibraryItemPagination {
    private final String buttonText;
    private final String urlPaginado;
    private boolean buttonSelected;

    public LibraryItemPagination(String buttonText, String urlPaginado, boolean buttonSelected) {
        this.buttonText = buttonText;
        this.urlPaginado = urlPaginado;
        this.buttonSelected = buttonSelected;
    }

    public String getButtonText() {
        return buttonText;
    }
    public String getUrlPaginado() {
        return urlPaginado;
    }
    public boolean getButtonSelected(){return buttonSelected;}
    public void setButtonSelected(boolean State){buttonSelected = State;}
}
