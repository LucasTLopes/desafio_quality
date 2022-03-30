package br.com.meli.desafio_quality;

import br.com.meli.desafio_quality.dto.DistrictDTO;
import br.com.meli.desafio_quality.dto.PropertyDTO;
import br.com.meli.desafio_quality.dto.RoomDTO;
import br.com.meli.desafio_quality.entity.Property;
import br.com.meli.desafio_quality.repository.PropertyRepository;
import br.com.meli.desafio_quality.service.PropertyService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PropertyIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyService propertyService;

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

    @AfterEach
    private void resetConfiguration() {
        propertyRepository.cleanAllProperties();
    }

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

        String response = postResult.getResponse().getContentAsString();
        PropertyDTO propertyDtoResponse = objectMapper.readValue(response, PropertyDTO.class);

        assertEquals("Tijuca Village", propertyDtoResponse.getName());

        Property property = propertyRepository.getProperty(propertyDtoResponse.getId());
        assertEquals("Tijuca Village", property.getName());

        Property propertyNull = propertyRepository.getProperty(propertyDtoResponse.getId()+"-XYZ12345-ABCD56789");
        assertNull(propertyNull.getName());
    }

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

//    @Test
//    public void insertInvalidRoomDimensionsProperty() throws Exception {
//        //TODO - Validate Invalid Insertions
//
//        DistrictDTO districtDTO = new DistrictDTO("Tijuca", BigDecimal.valueOf(15000));
//        List<RoomDTO> roomsDTO = Arrays.asList(new RoomDTO("kitchen", 10.0, 5.0),
//                new RoomDTO("living room", -2.0, -2.0));
//
//        PropertyDTO propertyDTO = new PropertyDTO(null, "Tijuca Village", districtDTO, roomsDTO);
//
//        MvcResult postResult = mockMvc.perform(post("/property/insert")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(propertyDTO)))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//    }
}
