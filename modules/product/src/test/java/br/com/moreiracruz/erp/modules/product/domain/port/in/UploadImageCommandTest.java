package br.com.moreiracruz.erp.modules.product.domain.port.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UploadImageCommandTest {

    @Test
    void equalityAndHashCodeUseContentBytes() {
        UploadImageCommand first = new UploadImageCommand("front.jpg", "image/jpeg", 3, new byte[]{1, 2, 3});
        UploadImageCommand second = new UploadImageCommand("front.jpg", "image/jpeg", 3, new byte[]{1, 2, 3});

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void contentIsDefensivelyCopied() {
        byte[] content = new byte[]{1, 2, 3};
        UploadImageCommand command = new UploadImageCommand("front.jpg", "image/jpeg", 3, content);

        content[0] = 9;
        byte[] exposed = command.content();
        exposed[1] = 9;

        assertThat(command.content()).containsExactly(1, 2, 3);
        assertThat(command.toString()).contains("contentLength=3");
    }
}
