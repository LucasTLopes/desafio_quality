package br.com.meli.desafio_quality;

import br.com.meli.desafio_quality.dto.*;
import br.com.meli.desafio_quality.entity.District;
import br.com.meli.desafio_quality.entity.Property;
import br.com.meli.desafio_quality.repository.PropertyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Classe responsável pelos testes de integração dos endpoints do PropertyController.
 * @author Jederson Macedo
 * @author Igor Nogueira
 * @author Luís Felipe Olimpio
 * @author Arthur Guedes
 * @author Lucas Troleiz
 * @author Jeferson Barbosa
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PropertyIntegrationTest {

    /**
     * {@link MockMvc mockMvc} Servelet mockado do Spring para realizar operações Http nas rotas do controller
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * {@link ObjectMapper objectMapper} Classe para serializar e desserializar obejtos
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * {@link PropertyRepository Repository} repositório que está sendo mockado
     */
    @Autowired
    private PropertyRepository propertyRepository;

    /**
     * Metodo para preparar o ambiente de cada teste individualmente
     */
    @BeforeEach
    private void initConfiguration() throws Exception {
        DistrictDTO districtDTO1 = new DistrictDTO("Barra da Tijuca", BigDecimal.valueOf(18000));
        DistrictDTO districtDTO2 = new DistrictDTO("Alphaville", BigDecimal.valueOf(14000));

        List<RoomDTO> roomsDTO1 = Arrays.asList(new RoomDTO("Kitchen", 10.0, 5.0),
                new RoomDTO("Living room", 20.0, 5.0));

        List<RoomDTO> roomsDTO2 = Arrays.asList(new RoomDTO("Kitchen", 10.0, 4.0),
                new RoomDTO("Living room", 15.0, 5.0),
                new RoomDTO("Bedroom", 5.0, 5.0));

        PropertyDTO propertyDTO1 = new PropertyDTO(null, "Brooklyn Village", districtDTO1, roomsDTO1);
        PropertyDTO propertyDTO2 = new PropertyDTO(null, "Moema Palace", districtDTO2, roomsDTO2);

        mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propertyDTO1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propertyDTO2)))
                .andExpect(status().isCreated());
    }

    /**
     * Resetando o ambiente após cada teste
     */
    @AfterEach
    private void resetConfiguration() {
        propertyRepository.cleanAllProperties();
    }

    private List<PropertyDTO> getAllProperties() throws Exception {
        MvcResult getResult = mockMvc.perform(get("/property/get-all-properties"))
                .andExpect(status().isOk())
                .andReturn();

        String response = getResult.getResponse().getContentAsString();
        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    /**
     * Verifica se a propriedade esta sendo criada corretamente
     */
    @Test
    public void insertPropertyAndCheckDto() throws Exception {
        DistrictDTO districtDTO = new DistrictDTO("Barra da Tijuca", BigDecimal.valueOf(15000));
        List<RoomDTO> roomsDTO = Arrays.asList(new RoomDTO("Kitchen", 10.0, 5.0),
                new RoomDTO("Living Room", 20.0, 5.0));

        PropertyDTO propertyDTO = new PropertyDTO(null, "Tijuca Village", districtDTO, roomsDTO);

        MvcResult postResult = mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = postResult.getResponse().getContentAsString(UTF_8);
        PropertyDTO propertyDtoResponse = objectMapper.readValue(response, PropertyDTO.class);

        assertEquals("Tijuca Village", propertyDtoResponse.getName());

        Property property = propertyRepository.getProperty(propertyDtoResponse.getId());
        assertEquals("Tijuca Village", property.getName());
    }

    /**
     * Valida se todas as propriedades criadas no @Beforeach estao retornando corretamente.
     */
    @Test
    public void getAllPropertiesTest() throws Exception{
        MvcResult getResult = mockMvc.perform(get("/property/get-all-properties"))
                .andExpect(status().isOk())
                .andReturn();

        String response = getResult.getResponse().getContentAsString();
        List<PropertyDTO> propertyDtoResponseList = objectMapper.readValue(response, new TypeReference<>() {
        });

        int propertyListSize = propertyDtoResponseList.size();
        assertEquals(2, propertyListSize);

    }
    /**
     * Valida o retorno do endpoint de calculo de area de propriedade.
     */
    @Test
    public void calculateTotalAreaTest() throws Exception{

        List<PropertyDTO> propertyDtoResponseList = getAllProperties();
        PropertyDTO propertyDTO = propertyDtoResponseList.get(0);

        MvcResult getResult = mockMvc.perform(get("/property/calculate-total-area-property/{propertyId}", propertyDTO.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String response = getResult.getResponse().getContentAsString();
        PropertyTotalAreaDTO propertyTotalAreaDTO = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals(150.0, propertyTotalAreaDTO.getTotalArea());

    }
    /**
     * Valida o retorno do endpoint que busca o maior comodo de determinada propriedade
     */
    @Test
    public void findLargestRoomTest() throws Exception{

        List<PropertyDTO> propertyDtoResponseList = getAllProperties();
        PropertyDTO propertyDTO = propertyDtoResponseList.get(1);

        MvcResult getResult = mockMvc.perform(get("/property/find-largest-room/{propertyId}", propertyDTO.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String response = getResult.getResponse().getContentAsString();
        LargestRoomAreaDTO largestRoomAreaDTO = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals(75.0, largestRoomAreaDTO.getTotalArea());
        assertEquals("Living room", largestRoomAreaDTO.getRoomName());

    }

    /**
     * Valida o retorno do endpoint que  calcula a area de cada comodo de uma propriedade
     */
    @Test
    public void calculateAreaRoomsTest() throws Exception{

        List<PropertyDTO> propertyDtoResponseList = getAllProperties();
        PropertyDTO propertyDTO = propertyDtoResponseList.get(0);

        MvcResult getResult = mockMvc.perform(get("/property/calculate-area-rooms/{propertyId}", propertyDTO.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String response = getResult.getResponse().getContentAsString();
        RoomAreasDTO roomAreasDTO = objectMapper.readValue(response, new TypeReference<>() {});

        Map<String, Double> listExpected = Map.of(
                "Kitchen", 50.0,
                "Living room", 100.0
        );
        assertEquals(listExpected, roomAreasDTO.getRoomAreas());
    }
    /**
     * Valida o retorno do endpoint que  calcula o valor de uma propriedade
     */
    @Test
    public void calculatePropertyPriceTest() throws Exception{

        List<PropertyDTO> propertyDtoResponseList = getAllProperties();
        PropertyDTO propertyDTO = propertyDtoResponseList.get(0);

        MvcResult getResult = mockMvc.perform(get("/property/calculate-property-price/{propertyId}", propertyDTO.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String response = getResult.getResponse().getContentAsString();
        PropertyPriceDTO propertyPriceDTO = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals(BigDecimal.valueOf(2700000.0), propertyPriceDTO.getPrice());
    }
    /**
     * Valida a exceção ao inserir dimensoes maiores que as permitidas
     */
    @Test
    public void insertInvalidRoomDimensionsProperty() throws Exception {

        DistrictDTO districtDTO = new DistrictDTO("Tijuca", BigDecimal.valueOf(15000));
        List<RoomDTO> roomsDTO = Arrays.asList(new RoomDTO("kitchen", 10.0, 5.0),
                new RoomDTO("living room", -2.0, -2.0));

        PropertyDTO propertyDTO = new PropertyDTO(null, "Tijuca Village", districtDTO, roomsDTO);

        MvcResult postResult = mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String result = postResult.getResponse().getContentAsString(UTF_8);
        ErrorDTO errorDTO = objectMapper.readValue(result, new TypeReference<>() {});

        assertEquals("O nome do cômodo deve começar com uma letra maiúscula.", errorDTO.getDescription());

    }

    /**
     * Valida a exceção ao inserir uma propriedade com bairro inexistente
     */
    @Test
    public void insertPropertyWithoutExistentDistrict() throws Exception {
        DistrictDTO districtDTO = new DistrictDTO("Random", BigDecimal.valueOf(15000));
        List<RoomDTO> roomsDTO = Arrays.asList(new RoomDTO("Kitchen", 10.0, 5.0),
                new RoomDTO("Living", 2.0, 2.0));

        PropertyDTO propertyDTO = new PropertyDTO(null, "Random", districtDTO, roomsDTO);

        MvcResult postResult = mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String result = postResult.getResponse().getContentAsString(UTF_8);
        ErrorDTO errorDTO = objectMapper.readValue(result, new TypeReference<>() {});

        assertEquals("o bairro Random não está cadastrado.", errorDTO.getDescription());
    }

    /**
     * Valida o formato do JSON  as inserir uma nova propriedade
     */
    @Test
    public void insertPropertyWithBadFormatting() throws Exception {
        String randomString = "{\n" +
                "    \"name\": \"Barra da Tijuca XYZ\",\n" +
                "    \"district\": {\n" +
                "        \"name\": \"Condominio dos ricos\",\n" +
                "        \"valueDistrictM2\": 100\n" +
                "    },";


        MvcResult postResult = mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(randomString))
                .andExpect(status().isBadRequest())
                .andReturn();

        String result = postResult.getResponse().getContentAsString(UTF_8);
        ErrorDTO errorDTO = objectMapper.readValue(result, new TypeReference<>() {});

        assertEquals("HttpMessageNotReadableException", errorDTO.getName());
    }

    /**
     * Valida se existe bairro ao inserir uma nova propriedade
     */
    @Test
    public void insertPropertyWithoutDistrict() throws Exception {
        District district = new District();
        DistrictDTO districtDTO = DistrictDTO.districtToDTO(district);
        List<RoomDTO> roomsDTO = Arrays.asList(new RoomDTO("Kitchen", 10.0, 5.0),
                new RoomDTO("Living", 2.0, 2.0));

        PropertyDTO propertyDTO = new PropertyDTO(null, "Random", districtDTO, roomsDTO);

        MvcResult postResult = mockMvc.perform(post("/property/insert")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String result = postResult.getResponse().getContentAsString(UTF_8);
        ErrorDTO errorDTO = objectMapper.readValue(result, new TypeReference<>() {});

        assertEquals("DistrictNotFoundException", errorDTO.getName());
    }
    /**
     * Valida se ocorre uma  PropertyNotFoundException quando um ID inexistente é requisitado
     */
    @Test
    public void getPropertyTotalAreaWithoutValidId() throws Exception {
        MvcResult getResult = mockMvc.perform(get("/property/calculate-property-price/{propertyId}", "XYZ12345-ABCD56789"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = getResult.getResponse().getContentAsString(UTF_8);
        ErrorDTO error = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals("o ID: XYZ12345-ABCD56789 não está cadastrado.", error.getDescription());
    }
}
