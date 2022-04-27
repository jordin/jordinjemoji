package in.jord.jordinjemoji;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import in.jord.jordinjemoji.rasterisation.EmojiRasteriser;
import in.jord.jordinjemoji.util.StandardUnassignedUnicodeRegion;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.apache.batik.transcoder.TranscoderException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

import static in.jord.jordinjemoji.util.UnicodeUtil.codePointsToUnicode;

public final class EmojiManager {
    private static final String BASE_DIRECTORY = "twemoji/assets/svg";

    private final int baseCodePoint;
    private final int emojiCount;

    private final Function<String, InputStream> resourceAsStream;
    private final Map<String, String> unicodeToCodePoint;
    private final List<String> codePointToResource;
    private final EmojiRasteriser rasteriser = new EmojiRasteriser(this);

    public EmojiManager(final Function<String, InputStream> resourceAsStream,
                        final Map<String, String> unicodeToCodePoint,
                        final List<String> codePointToResource,
                        final int baseCodePoint) {
        if (unicodeToCodePoint.size() != codePointToResource.size()) {
            throw new IllegalArgumentException("Unicode to code point map and the code point to resource list must have the same cardinality.");
        }
        this.resourceAsStream = resourceAsStream;
        this.unicodeToCodePoint = unicodeToCodePoint;
        this.codePointToResource = codePointToResource;
        this.baseCodePoint = baseCodePoint;
        this.emojiCount = codePointToResource.size();
    }

    public EmojiManager(final Function<String, InputStream> resourceAsStream, final int baseCodePoint) {
        this.resourceAsStream = resourceAsStream;
        this.unicodeToCodePoint = new HashMap<>();
        this.codePointToResource = new ArrayList<>();
        this.baseCodePoint = baseCodePoint;
        this.emojiCount = this.scanForEmoji();
    }

    public EmojiManager(final Function<String, InputStream> resourceAsStream, final StandardUnassignedUnicodeRegion region) {
        this(resourceAsStream, region.getBaseCodePoint());
    }

    public EmojiManager(final Function<String, InputStream> resourceAsStream) {
        this(resourceAsStream, StandardUnassignedUnicodeRegion.SUPPLEMENTARY_PRIVATE_USE_AREA_A);
    }

    public EmojiManager() {
        this(name -> EmojiManager.class.getClassLoader().getResourceAsStream(BASE_DIRECTORY + "/" + name));
    }

    public String convertEmojiToCodePoints(final String input) {
        return EmojiParser.parseFromUnicode(EmojiParser.parseToUnicode(input), unicodeCandidate -> {
            final Emoji emoji = unicodeCandidate.getEmoji();
            return this.unicodeToCodePoint.get(emoji.supportsFitzpatrick() ?
                    emoji.getUnicode(unicodeCandidate.getFitzpatrick()) :
                    emoji.getUnicode()
            );
        });
    }

    public InputStream getResourceAsStream(final String name) {
        return this.resourceAsStream.apply(name);
    }

    public String getResource(final int codePoint) {
        if (!this.hasCodePoint(codePoint)) {
            throw new IllegalArgumentException("Invalid code point.");
        }
        return this.codePointToResource.get(codePoint - this.baseCodePoint);
    }

    public BufferedImage rasterise(final int codePoint, final int width, final int height) throws IOException, TranscoderException {
        return this.rasteriser.rasteriseEmoji(this.getResource(codePoint), width, height);
    }

    public boolean hasCodePoint(final int codePoint) {
        return codePoint >= this.baseCodePoint && codePoint < (this.baseCodePoint + this.emojiCount);
    }

    public int getBaseCodePoint() {
        return this.baseCodePoint;
    }

    public int getEmojiCount() {
        return this.emojiCount;
    }

    private static ScanResult scanClassGraph(final ClassGraph classGraph) {
        final Integer threads = Integer.getInteger("jordinjemoji.threads");

        if (threads != null) {
            return classGraph.scan(threads);
        }

        return classGraph.scan();
    }

    private int scanForEmoji() {
        int emojiCount = 0;

        final ClassGraph classGraph = new ClassGraph()
                .acceptPathsNonRecursive(BASE_DIRECTORY);

        try (final ScanResult scanResult = EmojiManager.scanClassGraph(classGraph)) {
            for (final Resource resource : scanResult.getAllResources()) {
                final String resourceName = resource.getPath();

                if (resourceName.endsWith(".svg")) {
                    final int lastSlashIndex = resourceName.lastIndexOf('/');
                    final int lastDotIndex = resourceName.lastIndexOf('.');

                    if (lastDotIndex > lastSlashIndex) {
                        final String codepoints = resourceName.substring(lastSlashIndex + 1, lastDotIndex);
                        final String unicodeRepresentation = codePointsToUnicode(codepoints);

                        final int codePoint = this.baseCodePoint + emojiCount;

                        this.unicodeToCodePoint.put(unicodeRepresentation, Character.toString(codePoint));
                        this.codePointToResource.add(resourceName.substring(lastSlashIndex + 1));
                        emojiCount++;
                    }
                }
            }
        }

        return emojiCount;
    }
}
