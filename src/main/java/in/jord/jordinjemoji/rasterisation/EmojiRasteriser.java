package in.jord.jordinjemoji.rasterisation;

import in.jord.jordinjemoji.EmojiManager;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class EmojiRasteriser {
    private final EmojiManager emojiManager;

    public EmojiRasteriser(final EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    public final BufferedImage rasteriseEmoji(final String name, final int width, final int height) throws IOException, TranscoderException {
        try (final InputStream emoji = this.emojiManager.getResourceAsStream(name)) {
            return rasteriseSvg(emoji, width, height);
        }
    }

    public static BufferedImage rasteriseSvg(final InputStream inputStream, final int width, final int height) throws TranscoderException {
        final BufferedImageTranscoder transcoder = new BufferedImageTranscoder(width, height);

        final TranscoderInput input = new TranscoderInput(inputStream);
        final BufferedImageTranscoderOutput output = new BufferedImageTranscoderOutput();

        transcoder.transcode(input, output);

        return output.getImage();
    }
}
