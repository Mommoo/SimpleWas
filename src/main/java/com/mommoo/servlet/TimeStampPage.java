package com.mommoo.servlet;

import com.mommoo.http.request.HttpRequest;
import com.mommoo.http.response.HttpResponse;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

/**
 * 현재 시간 스탬프를 출력하는 서블릿 클래스 입니다.
 *
 * @author mommoo
 */
public class TimeStampPage implements SimpleServlet {
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Writer writer = httpResponse.getWriter();

        String timeStamp = simpleDateFormat.format(System.currentTimeMillis());
        try {
            writer.append("<html>")
                    .append("<head><title>TimeStampPage</title></head>")
                    .append("<body>")
                    .append("<center><h1>").append(timeStamp).append("</h1></center>")
                    .append("</body>")
                    .append("</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
