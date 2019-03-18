package com.mommoo.http;

public enum HttpStatus {
    CODE_200(200, "OK"),
    CODE_403(403, "Forbidden"),
    CODE_404(404, "Not Found"),
    CODE_412(412, "Precondition Failed"),
    CODE_500(500, "Internal Server Error");

    private final int codeNum;
    private final String message;

    private HttpStatus(int codeNum, String message) {
        this.codeNum = codeNum;
        this.message = message;
    }

    public int getCodeNum() {
        return codeNum;
    }

    @Override
    public String toString() {
        return codeNum + " " + message;
    }
}
