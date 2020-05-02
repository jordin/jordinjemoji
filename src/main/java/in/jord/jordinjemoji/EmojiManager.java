package in.jord.jordinjemoji;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import in.jord.jordinjemoji.rasterisation.EmojiRasteriser;
import org.apache.batik.transcoder.TranscoderException;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static in.jord.jordinjemoji.util.UnicodeUtil.codePointsToUnicode;

public final class EmojiManager {
    private static final String BASE_DIRECTORY = "twemoji/assets/svg/";
    public static final int PRIVATE_USE_AREA_A_START = 0xF0000;
    public static final int PRIVATE_USE_AREA_B_START = 0x100000;

    private final int baseCodePoint;
    private final int emojiCount;

    private final Function<String, InputStream> resourceAsStream;
    private final Map<String, String> unicodeToCodePoint;
    private final List<String> codePointToResource;
    private final EmojiRasteriser rasteriser = new EmojiRasteriser(this);

    public EmojiManager() throws IOException {
        this(name -> EmojiManager.class.getClassLoader().getResourceAsStream(BASE_DIRECTORY + name));
    }

    public EmojiManager(final Function<String, InputStream> resourceAsStream) throws IOException {
        this.resourceAsStream = resourceAsStream;
        this.unicodeToCodePoint = new HashMap<>();
        this.codePointToResource = new ArrayList<>();
        this.baseCodePoint = PRIVATE_USE_AREA_A_START;
        this.emojiCount = this.scanForEmoji();
    }

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

    private int scanForEmoji() throws IOException {
        int emojiCount = 0;

        try (final InputStream directory = this.getResourceAsStream("")) {
            assert directory != null;

            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(directory, StandardCharsets.UTF_8))) {
                String resourceName;

                while ((resourceName = reader.readLine()) != null) {
                    final int codePoint = this.baseCodePoint + emojiCount;
                    final String unicodeRepresentation = codePointsToUnicode(resourceName.substring(0, resourceName.length() - ".svg".length()));

                    this.unicodeToCodePoint.put(unicodeRepresentation, Character.toString(codePoint));
                    this.codePointToResource.add(resourceName);

                    emojiCount++;
                }
            }
        }

        return emojiCount;
    }
}
