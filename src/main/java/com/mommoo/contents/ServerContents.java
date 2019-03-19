package com.mommoo.contents;

import com.mommoo.http.HttpStatus;

/**
 * {@link ServerContentsFinder} 인스턴스가 컨텐츠를 찾은 결과를 정의한 클래스입니다.
 *
 * {@link #contents}는 {@link ServerContentsType} 에 따라 다르게 다운캐스팅 되어야 합니다.
 *
 *  1. {@link ServerContentsType#NONE} : {@link #contents}는 NULL 입니다.
 *  2. {@link ServerContentsType#FILE} : {@link #contents}는 {@link java.nio.file.Path} 입니다.
 *  3. {@link ServerContentsType#SERVLET} : {@link #contents}는 {@link com.mommoo.servlet.SimpleServlet} 입니다.
 *
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
