import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class HtmlMapper {

//    private static Logger LOGGER = LoggerFactory.getLogger(HtmlMapper.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case
        String resourcePath = "./samples/startbootstrap-freelancer-gh-pages-cut.html";
        String targetElementId = "sendMessageButton";

        Optional<Element> buttonOpt = findElementById(new File(resourcePath), targetElementId);

        Optional<String> stringifiedAttributesOpt = buttonOpt.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                        .collect(Collectors.joining(", "))
        );

        stringifiedAttributesOpt.ifPresent(attrs -> System.out.println(attrs));
    }

    private static Optional<Element> findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return Optional.of(doc.getElementById(targetElementId));

        } catch (IOException e) {
            System.out.println("Error reading  " + htmlFile.getAbsolutePath() + "file");
            return Optional.empty();
        }
    }

}