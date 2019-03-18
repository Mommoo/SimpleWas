package com.mommoo.contents;

import com.mommoo.conf.ServerSpec;
import com.mommoo.conf.ServerSpecBuilder;
import com.mommoo.http.HttpStatus;
import com.mommoo.servlet.TimeStampPage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;

/**
 * 클라이언트가 요청한 URI에 맞는 컨텐츠를 찾는 로직을 검증하는 테스트 클래스 입니다.
 * 검증 클래스 대상은 {@link ServerContentsFinder} 이며, 테스트 케이스는 크게 3가지로 구성했습니다.
 *
 *  1. 올바른 URI를 받았을 때, 요구하는 컨텐츠가 존재하는지 여부 테스트. {@link #findProperContentsIfValidURI()}
 *  2. 올바르지 않은 URI를 받아 에러 상태이며, 에러 페이지를 매핑할 수 있는지 여부 테스트. {@link #findProperContentsIfErrorStatus()}
 *  3. 서블릿 URI를 받았을 때, 요구한 서블릿을 찾을 수 있는지 여부 테스트. {@link #findProperContentsIfRequestServlet()}
 *
 *  서블릿 테스트는 {@link com.mommoo.servlet.TimeStampPage} 로 테스트 합니다.
 */
public class ServerContentsFinderTest {

    private static ServerSpec createMockServerSpec() {
        return new ServerSpecBuilder()
                .setDocumentPath("var/www")
                .setLogPath("log")
                .setIndexPage("index.html")
                .setPortNumber(6766)
                .setServerName("mommoo.com")
                .addErrorPage(403, "error403.html")
                .build();
    }

    @Test
    @DisplayName("올바른 URI를 받았을때, 요구 컨텐츠 처리 테스트")
    public void findProperContentsIfValidURI() throws IOException {
        ServerSpec mockServerSpec = createMockServerSpec();

        // 클라이언트 요청 URI가 '/' 이며, 올바른 요청 (200 OK) 이라 가정합니다.
        // 최상위 URI '/'를 요구했으므로, index page를 처리해야 합니다.
        ServerContentsFinder serverContentsFinder = new ServerContentsFinder(mockServerSpec, "/", HttpStatus.CODE_200);
        ServerContents serverContents = serverContentsFinder.getContents();

        // indexPage 파일이 존재하지 않으므로, ServerContents는 NONE 이어야 합니다.
        Assertions.assertSame(serverContents.getType(), ServerContentsType.NONE);
        // 초기에는 200 상태 였지만, 찾을 컨텐츠가 존재하지 않으므로 404 상태입니다.
        Assertions.assertSame(serverContents.getHttpStatus(), HttpStatus.CODE_404);

        // indexPage 파일을 임의로 생성 합니다.
        Path directoryPath = Paths.get(mockServerSpec.getDocumentPath());
        Path path = Paths.get(mockServerSpec.getDocumentPath(), mockServerSpec.getIndexPage());
        Files.createDirectories(directoryPath);
        Files.createFile(path);

        // 다시 같은 조건으로 Finder를 생성합니다. (클라이언트 요청 URI가 '/' 이며, 올바른 요청 (200 OK) 이라 가정합니다.)
        serverContentsFinder = new ServerContentsFinder(mockServerSpec, "/", HttpStatus.CODE_200);
        serverContents = serverContentsFinder.getContents();

        // indexPage의 파일을 생성 했으므로, ServerContents는 File 이어야 합니다.
        Assertions.assertSame(serverContents.getType(), ServerContentsType.FILE);
        // 올바른 File 컨텐츠가 존재하므로, 200 상태 값이 유지되어야 합니다.
        Assertions.assertSame(serverContents.getHttpStatus(), HttpStatus.CODE_200);

        // indexPage의 Path가 올바르게 구해졌는지 확인합니다.
        Path computedPath = (Path) serverContents.get();
        boolean isPathTrue = path.toAbsolutePath().toString().equals(computedPath.toAbsolutePath().toString());
        Assertions.assertSame(isPathTrue, true);

        // index.html 파일을 직접 호출하는 URI를 구성하며("/index.html"), 올바른 요청 (200 OK) 이라 가정합니다.;
        serverContentsFinder = new ServerContentsFinder(mockServerSpec, "/index.html", HttpStatus.CODE_200);
        serverContents = serverContentsFinder.getContents();

        // indexPage파일이 존재하므로, ServerContents File이어야 합니다.
        Assertions.assertSame(serverContents.getType(), ServerContentsType.FILE);
        // 올바른 File 컨텐츠가 존재하므로, 200 상태 값으로 처리 되어야 합니다.
        Assertions.assertSame(serverContents.getHttpStatus(), HttpStatus.CODE_200);

        // indexPage의 Path가 올바르게 구해졌는지 확인합니다.
        computedPath = (Path) serverContents.get();
        isPathTrue = path.toAbsolutePath().toString().equals(computedPath.toAbsolutePath().toString());
        Assertions.assertSame(isPathTrue, true);
    }

    @Test()
    @DisplayName("HttpStatus가 에러 상태 일때, 처리 해야할 컨텐츠 검색 테스트")
    public void findProperContentsIfErrorStatus() throws IOException {
        ServerSpec mockServerSpec = createMockServerSpec();

        // 클라이언트 요청 URI가 '/mommoo/game.exe' 이라 가정합니다.
        // 요청 URI가 exe 파일 확장자를 요구하므로 403 상태 코드라 가정합니다.
        ServerContentsFinder serverContentsFinder = new ServerContentsFinder(mockServerSpec, "/mommoo/game.exe", HttpStatus.CODE_403);
        ServerContents serverContents = serverContentsFinder.getContents();

        // errorCode에 매핑되는 errorPage가 없으므로, ServerContents는 None이어야 합니다.
        Assertions.assertSame(serverContents.getType(), ServerContentsType.NONE);
        // 컨텐츠가 없으므로 404 에러도 가능하지만, 기존에 상위에러가 존재하므로 403 에러로 처리되어야 합니다.
        Assertions.assertSame(serverContents.getHttpStatus(), HttpStatus.CODE_403);

        // errorPage 파일을 임의로 생성 합니다.
        Path directoryPath = Paths.get(mockServerSpec.getDocumentPath());
        Path path = Paths.get(mockServerSpec.getDocumentPath(), mockServerSpec.getErrorPage().get(403));
        Files.createDirectories(directoryPath);
        Files.createFile(path);

        // 같은 조건으로 다시 만듭니다.
        // 클라이언트 요청 URI가 '/mommoo/game.exe' 이라 가정합니다.
        // 요청 URI가 exe 파일 확장자를 요구하므로 403 상태 코드라 가정합니다.
        serverContentsFinder = new ServerContentsFinder(mockServerSpec, "/mommoo/game.exe", HttpStatus.CODE_403);
        serverContents = serverContentsFinder.getContents();

        // errorCode에 매핑되는 errorPager가 있으므로, ServerContents는 File이어야 합니다.
        Assertions.assertSame(serverContents.getType(), ServerContentsType.FILE);
        // 기존 상위에러가 존재하므로 403 에러로 처리되어야 합니다.
        Assertions.assertSame(serverContents.getHttpStatus(), HttpStatus.CODE_403);

        // errorPage의 Path가 올바르게 구해졌는지 확인합니다.
        Path computedPath = (Path) serverContents.get();
        boolean isPathTrue = path.toAbsolutePath().toString().equals(computedPath.toAbsolutePath().toString());
        Assertions.assertSame(isPathTrue, true);
    }

    /** 해당 서블릿 테스트는 {@link com.mommoo.servlet.TimeStampPage} 로 테스트 합니다. */
    @Test
    @DisplayName("요청 URL이 서블릿일 때, 처리 해야할 컨텐츠 검색 테스트")
    public void findProperContentsIfRequestServlet() {
        ServerSpec mockServerSpec = createMockServerSpec();

        // 클라이언트 요청 URI가 '/com.mommoo.servlet.TimeStampPage' 이라 가정합니다.
        // 상태 코드는 200이라 가정합니다.
        ServerContentsFinder serverContentsFinder = new ServerContentsFinder(mockServerSpec, "/com.mommoo.servlet.TimeStampPage", HttpStatus.CODE_200);
        ServerContents serverContents = serverContentsFinder.getContents();

        // 요청한 URI에 맞는 서블릿 클래스가 존재하므로, 컨텐츠의 타입은 Servelt을 처리해야 합니다.
        Assertions.assertSame(serverContents.getType(), ServerContentsType.SERVLET);
        // 상태코드 또한 기존 200으로 처리되어야 합니다.
        Assertions.assertSame(serverContents.getHttpStatus(), HttpStatus.CODE_200);
        // 컨텐츠가 TimeStampPage 인스턴스 인지 확인합니다.
        boolean isTimeStampPageInstance = serverContents.get() instanceof TimeStampPage;
        Assertions.assertTrue(isTimeStampPageInstance);
    }

    /** 테스트를 위해 생성한 모든 파일을 삭제합니다.*/
    @AfterEach
    private void removeTestFiles() throws IOException{
        ServerSpec mockServerSpec = createMockServerSpec();

        // indexPage 삭제.
        Path path = Paths.get(mockServerSpec.getDocumentPath(), mockServerSpec.getIndexPage());
        if (Files.exists(path)) {
            Files.delete(path);
        }

        // 403 errorPage 삭제 합니다.
        Path errorPagePath
                = Paths.get(mockServerSpec.getDocumentPath(), mockServerSpec.getErrorIndexPageOrNull(HttpStatus.CODE_403));
        if (Files.exists(errorPagePath)) {
            Files.delete(errorPagePath);
        }

        // 테스트에 사용된 디렉토리를 전부 삭제합니다.
        while (true) {
            Path nextPath = path.getParent();
            if (nextPath==null) {
                break;
            }
            if (Files.exists(nextPath)) {
                Files.delete(nextPath);
            }
            path = nextPath;
        }
    }
}
