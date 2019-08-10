import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class HtmlMapper {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HtmlMapper.class);
    public static final String TARGET_ELEMENT_ID = "make-everything-ok-button";

    private static String CHARSET_NAME = "utf8";
    private static Map<Node, Integer> similarityMap = new HashMap<>();

    public static void main(String[] args) {

        String resourcePath = args[0];
        String diffFIlePath = args[1];

        Element buttonOpt = findElementById(new File(resourcePath), TARGET_ELEMENT_ID);

        if (buttonOpt != null) {
            Map<String, String> targetElementAttributes = buttonOpt.attributes().asList().stream()
                    .collect(toMap(Attribute::getKey, Attribute::getValue));

            String textNode = buttonOpt.childNode(0).toString();


            Element rootElement = getRootElement(new File(diffFIlePath));

            if (rootElement != null) {
                recursiveTraverse(rootElement, calculateNodeSimilarity(targetElementAttributes, textNode));

                Node key = similarityMap.entrySet().stream()
                        .max(comparingByValue()).get().getKey();

                log.info(key.toString());
            } else {
                log.error("Check html file structure!");
            }
        } else {
            log.error("No element by id {} found !", TARGET_ELEMENT_ID);
        }

    }

    private static Consumer<Node> calculateNodeSimilarity(Map<String, String> targetElementAttributes, String textNode) {
        return node -> {
            similarityMap.put(node, 0);
            node.attributes().forEach(attribute -> {
                String attrValue = targetElementAttributes.get(attribute.getKey());
                if (Objects.equals(attrValue, attribute.getValue())) {
                    incrimentNodeSimilarity(node);
                }
            });
            Optional<Node> tNode = node.childNodes().stream().filter(n -> n instanceof TextNode).findFirst();
            tNode.ifPresent((t) -> {
                if (Objects.equals(t.toString(), textNode)) {
                    incrimentNodeSimilarity(node);
                }
            });

        };
    }

    private static void incrimentNodeSimilarity(Node node) {
        Integer similarityCount = similarityMap.get(node);
        similarityCount++;
        similarityMap.put(node, similarityCount);
    }

    private static Element findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return doc.getElementById(targetElementId);

        } catch (IOException e) {
            log.error("Error reading  " + htmlFile.getAbsolutePath() + "file");
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
            log.error("Error reading  " + htmlFile.getAbsolutePath() + "file");
            return null;
        }


    }

    private static void recursiveTraverse(Node node, Consumer<Node> consumer) {
        for (Node currentNode : node.childNodes()) {
            consumer.accept(currentNode);
            recursiveTraverse(currentNode, consumer);
        }
    }

}