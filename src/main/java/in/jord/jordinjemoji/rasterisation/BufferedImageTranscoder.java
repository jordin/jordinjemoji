package in.jord.jordinjemoji.rasterisation;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

public final class BufferedImageTranscoder extends ImageTranscoder {
    public BufferedImageTranscoder(final int width, final int height) {
        this.hints.put(SVGAbstractTranscoder.KEY_HEIGHT, (float) height);
        this.hints.put(SVGAbstractTranscoder.KEY_WIDTH, (float) width);
    }

    @Override
    public BufferedImage createImage(final int width, final int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void writeImage(final BufferedImage image, final TranscoderOutput transcoderOutput) {
        if (transcoderOutput instanceof BufferedImageTranscoderOutput) {
            ((BufferedImageTranscoderOutput) transcoderOutput).setImage(image);
        }
    }
}
