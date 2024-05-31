import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.*;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class DocumentRequestValidatorTest {

    @Mock
    private CustomMetadataTypeRepository metadataTypeRepository;

    @InjectMocks
    private DocumentRequestValidator documentRequestValidator;

    private UUID documentTypeUUID;
    private List<MetadataDTO> reqMetadata;
    private List<MetadataType> supportedMetadataTypesForDocumentType;

    @BeforeEach
    void setUp() {
        documentTypeUUID = UUID.randomUUID();
        reqMetadata = List.of(
                new MetadataDTO("code1", "value1"),
                new MetadataDTO("code2", "value2")
        );
        supportedMetadataTypesForDocumentType = List.of(
                new MetadataType("code1", true, null, null),
                new MetadataType("code2", false, ".*", RegexTypeEnum.STRING)
        );
    }

    @Test
    void testValidateMetadataSuccess() {
        when(metadataTypeRepository.findAllByDocumentType(documentTypeUUID))
                .thenReturn(Flux.fromIterable(supportedMetadataTypesForDocumentType));

        Mono<Void> result = documentRequestValidator.validateMetadata(reqMetadata, documentTypeUUID);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testValidateMetadataWithNonSupportedType() {
        reqMetadata = List.of(new MetadataDTO("code3", "value3"));

        when(metadataTypeRepository.findAllByDocumentType(documentTypeUUID))
                .thenReturn(Flux.fromIterable(supportedMetadataTypesForDocumentType));

        Mono<Void> result = documentRequestValidator.validateMetadata(reqMetadata, documentTypeUUID);

        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void testValidateMetadataWithMandatoryTypeMissing() {
        supportedMetadataTypesForDocumentType = List.of(
                new MetadataType("code1", true, null, null)
        );

        when(metadataTypeRepository.findAllByDocumentType(documentTypeUUID))
                .thenReturn(Flux.fromIterable(supportedMetadataTypesForDocumentType));

        Mono<Void> result = documentRequestValidator.validateMetadata(reqMetadata, documentTypeUUID);

        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void testValidateMetadataWithInvalidRegex() {
        reqMetadata = List.of(new MetadataDTO("code2", "invalidValue"));

        when(metadataTypeRepository.findAllByDocumentType(documentTypeUUID))
                .thenReturn(Flux.fromIterable(supportedMetadataTypesForDocumentType));

        Mono<Void> result = documentRequestValidator.validateMetadata(reqMetadata, documentTypeUUID);

        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }
}
