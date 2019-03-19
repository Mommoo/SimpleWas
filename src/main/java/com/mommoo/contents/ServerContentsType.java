package com.mommoo.contents;

/**
 * 서버가 처리할 컨텐츠를 정의한 Enum 클래스 입니다.
 *
 * 추후 HttpRequest의 Accept를 해석하여, 파일 종류까지 디테일하게 처리하면 좋을거 같습니다.
 *
 * @author mommoo
 */
public enum ServerContentsType {
    SERVLET,
    FILE,
    NONE
}
