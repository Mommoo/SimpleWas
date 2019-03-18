package com.mommoo.servlet;

import com.mommoo.http.request.HttpRequest;
import com.mommoo.http.response.HttpResponse;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

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
