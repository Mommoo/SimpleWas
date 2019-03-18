package com.mommoo.contents;

import com.mommoo.http.HttpStatus;

/**
 * @author mommoo
 */
public class ServerContents {
    private final HttpStatus httpStatus;
    private final ServerContentsType contentsType;
    private final Object contents;

    public ServerContents(HttpStatus httpStatus, ServerContentsType contentsType, Object contents) {
        this.httpStatus = httpStatus;
        this.contentsType = contentsType;
        this.contents = contents;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ServerContentsType getType() {
        return contentsType;
    }

    public Object get() {
        return contents;
    }
}
