import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class HtmlMapper {

//    private static Logger LOGGER = LoggerFactory.getLogger(HtmlMapper.class);

    private static String CHARSET_NAME = "utf8";
    private static Map<Node, Integer> similarityMap = new HashMap<>();

    public static void main(String[] args) {

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case
        String resourcePath = "C:\\dev\\htmlmapper\\src\\main\\resources\\origin-file.html";
        String diffFIlePath = "C:\\dev\\htmlmapper\\src\\main\\resources\\diff3.html";
        String targetElementId = "make-everything-ok-button";

        Element buttonOpt = findElementById(new File(resourcePath), targetElementId);

        if (buttonOpt != null) {
            Map<String, String> targetElementAttributes = buttonOpt.attributes().asList().stream()
                    .collect(toMap(Attribute::getKey, Attribute::getValue));

            String textNode = buttonOpt.childNode(0).toString();


            Element elementsOpt = getRootElement(new File(diffFIlePath));

            recursiveTraverse(elementsOpt, node -> {
                similarityMap.put(node, 0);
                node.attributes().forEach(attribute -> {
                    String attrValue = targetElementAttributes.get(attribute.getKey());
                    if (Objects.equals(attrValue, attribute.getValue())) {
                        Integer similarityCount = similarityMap.get(node);
                        similarityCount++;
                        similarityMap.put(node, similarityCount);
                    }
                });
                Optional<Node> tNode = node.childNodes().stream().filter(n -> n instanceof TextNode).findFirst();
                tNode.ifPresent((t) -> {
                    if (Objects.equals(t.toString(), textNode)) {
                        Integer similarityCount = similarityMap.get(node);
                        similarityCount++;
                        similarityMap.put(node, similarityCount);
                    }
                });

            });

            Node key = similarityMap.entrySet().stream()
                    .max(comparingByValue()).get().getKey();

            System.out.println(key.toString());

        } else {
            System.out.println("No element by id " + targetElementId + "found !");
        }

    }

    private static Element findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return doc.getElementById(targetElementId);

        } catch (IOException e) {
            System.out.println("Error reading  " + htmlFile.getAbsolutePath() + "file");
            return null;
        }
    }

    private static Element getRootElement(File htmlFile) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());
            return doc.body();

        } catch (IOException e) {
            System.out.println("Error reading  " + htmlFile.getAbsolutePath() + "file");
            return null;
        }


    }

    private static void recursiveTraverse(Node doc, Consumer<Node> consumer) {
        for (Node el : doc.childNodes()) {
            consumer.accept(el);
            recursiveTraverse(el, consumer);
        }
    }

}