package com.project.roomly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.roomly.dto.Media.ResponseRoomMediaDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.SearchRoomsDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.entity.Hotel;
import com.project.roomly.entity.HotelMedia;
import com.project.roomly.entity.Room;
import com.project.roomly.entity.RoomMedia;
import com.project.roomly.repository.HotelMediaRepository;
import com.project.roomly.repository.HotelRepository;
import com.project.roomly.repository.RoomMediaRepository;
import com.project.roomly.repository.RoomRepository;
import com.project.roomly.scheduler.MediaScheduler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class RoomlyApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private HotelMediaRepository hotelMediaRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RoomMediaRepository roomMediaRepository;

	@MockitoBean
	private S3Client s3Client;

	@MockitoBean
	private MediaScheduler mediaScheduler;


	private Hotel testHotel;

	private Room testRoom;

	@BeforeEach
	@Transactional
	void setupHotel() {
		testHotel = new Hotel();
		testHotel.setName("Test Hotel");
		testHotel.setAddress("Test Address");
		testHotel.setOwner(UUID.randomUUID());
		testHotel.setPrepaymentPercentage(10);
		testHotel.setCity("Москва");
		HotelMedia hotelMedia = new HotelMedia("https://test/image.png", testHotel);
		testHotel.setMedia(List.of(hotelMedia));


		testHotel = hotelRepository.save(testHotel);
		testRoom = new Room();
		testRoom.setHotel(testHotel);
		testRoom.setCountRoom(10);
		testRoom.setPriceDay(BigDecimal.valueOf(1000));
		testRoom.setName("Номер тест");
		testRoom.setDescription("Описание номера");
		testRoom.setFloor(1);
		RoomMedia roomMedia = new RoomMedia("https://test/image.png", testRoom);
		testRoom.setMedia(List.of(roomMedia));
		roomRepository.save(testRoom);
		roomMediaRepository.save(roomMedia);
	}



	@Test
	@DisplayName("ПРОВЕРКА POST /api/v1/room")
	void testCreateRoom() throws Exception {
		when(s3Client.putObject( any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);

		RoomDto roomDto = new RoomDto("комната", testHotel.getId(), "Описание комнаты", 10, 10, BigDecimal.valueOf(1200));

		String roomJson = objectMapper.writeValueAsString(roomDto);

		MockMultipartFile roomPart = new MockMultipartFile(
				"room",
				"room.json",
				"application/json",
				roomJson.getBytes()
		);

		MockMultipartFile mediaPart1 = new MockMultipartFile(
				"media",
				"image1.png",
				"image/png",
				"test image content".getBytes()
		);

		MockMultipartFile mediaPart2 = new MockMultipartFile(
				"media",
				"image2.jpg",
				"image/jpeg",
				"another test image".getBytes()
		);


		mockMvc.perform(MockMvcRequestBuilders
				.multipart("/api/v1/room")
				.file(roomPart)
				.file(mediaPart1)
				.file(mediaPart2)
				.with(jwt().jwt(builder-> {
					builder.claim("sub", testHotel.getOwner().toString());
				}))
		).andExpect(status().isCreated());

		Assertions.assertTrue(roomRepository.findAll().size() > 1);
	}

	@Test
	@DisplayName("ПРОВЕРКА PATCH /api/v1/room")
	void testUpdateRoom() throws Exception {

		SetRoomDto setRoomDto = new SetRoomDto(testRoom.getId(), "Люкс 3х комнатный", "Лучший номер в стране", 10, 5, BigDecimal.valueOf(1003.5d));

		mockMvc.perform(MockMvcRequestBuilders
				.patch("/api/v1/room")
				.with(jwt().jwt(builder-> {
					builder.claim("sub", testHotel.getOwner().toString());
				}))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(setRoomDto))
		).andExpect(status().isOk());

		Optional<Room> setRoomTestOptional = roomRepository.findById(testRoom.getId());
		Assertions.assertFalse(setRoomTestOptional.isEmpty());
		Room setRoom = setRoomTestOptional.get();
        Assertions.assertEquals(setRoom.getCountRoom(), setRoomDto.countRoom());
		Assertions.assertEquals(setRoom.getName(), setRoomDto.name());
		Assertions.assertEquals(setRoom.getFloor(), setRoomDto.floor());
		Assertions.assertEquals(setRoom.getDescription(), setRoomDto.description());
		Assertions.assertTrue(setRoom.getPriceDay().compareTo(setRoomDto.priceDay()) == 0);
	}

	@Test
	@DisplayName("ПРОВЕРКА GET /api/v1/room/{id}")
	void testGetRoom() throws Exception {
		ResponseRoomMediaDto responseRoomMediaDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders
				.get("/api/v1/room/" + testRoom.getId())
				.with(jwt().jwt(builder-> {
					builder.claim("sub", testHotel.getOwner().toString());
				}))
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ResponseRoomMediaDto.class);
		System.out.println(responseRoomMediaDto);
		System.out.println(testRoom.getMedia());
		Assertions.assertEquals(responseRoomMediaDto.room().countRoom(), testRoom.getCountRoom());
		Assertions.assertEquals(responseRoomMediaDto.room().priceDay().compareTo(testRoom.getPriceDay()), 0);
		Assertions.assertEquals(responseRoomMediaDto.room().prepaymentPercentage(), testRoom.getHotel().getPrepaymentPercentage());
		Assertions.assertEquals(responseRoomMediaDto.room().name(), testRoom.getName());
		Assertions.assertEquals(responseRoomMediaDto.room().description(), testRoom.getDescription());
		Assertions.assertEquals(responseRoomMediaDto.room().floor(), testRoom.getFloor());
		Assertions.assertEquals(responseRoomMediaDto.room().hotelId(), testRoom.getHotel().getId());
		Assertions.assertEquals(responseRoomMediaDto.media().size(), roomMediaRepository.countMediaByRoom(testRoom.getId()));
	}

	@Test
	@DisplayName("ПРОВЕРКА POST /api/v1/room/{id}")
	void testAddMedia() throws Exception {
		when(s3Client.putObject( any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
		String nameFile = "testImage";
		MockMultipartFile mediaPart1 = new MockMultipartFile(
				"media",
				nameFile,
				"image/jpeg",
				"another test image".getBytes()
		);

		mockMvc.perform(MockMvcRequestBuilders
				.multipart("/api/v1/room/" + testRoom.getId())
				.file(mediaPart1)
				.with(jwt().jwt(builder-> {
					builder.claim("sub", testHotel.getOwner().toString());
				}))
		).andExpect(status().isCreated());

		List<String> mediaKeys = roomMediaRepository.findMediaByRoomId(testRoom.getId());
		Assertions.assertFalse(mediaKeys.isEmpty());
		Assertions.assertTrue(mediaKeys.stream().anyMatch(url -> url.contains(".jpeg")));
	}

	/*
	Удаления медиа отсрочивается на срок работы обработчика, проверим удаления медиа как поиск null значений в таблице RoomMedia.
	(Media deletion is delayed for the duration of the handler, let's check media deletion as a search for null values in the RoomMedia table.)
	 */
	@Test
	@DisplayName("ПРОВЕРКА DELETE /api/v1/room/{id}")
	@Transactional
	void testDeleteRoom() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
				.delete("/api/v1/room/" + testRoom.getId())
				.with(jwt().jwt(builder ->
						builder.claim("sub", testHotel.getOwner().toString())))

		).andExpect(status().isOk());
		Assertions.assertTrue(roomRepository.findById(testRoom.getId()).isPresent());
		System.out.println(roomMediaRepository.findAll());
		Assertions.assertTrue(roomMediaRepository.deleteByRoomIsNull() > 0);
	}

	/*
	Поиск осуществляется по нескольким критериям а именно цена, дата, город. Проверим нашлось ли совпадения с тестовым номером.
	The search is based on several criteria, namely price, date, city. Let's check if there is a match with the test number.
	 */
	@Test
	@DisplayName("ПРОВЕРКА GET /api/v1/search")
	void testSearchRoom() throws Exception{
		SearchRoomsDto searchRoomsDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders
					.get("/api/v1/room/search")
					.param("city", "Москва")
					.param("startDate", "2025-09-12")
					.param("endDate", "2025-09-15")
					.param("page", "0")
					.param("minPrice", testRoom.getPriceDay().toString())
					.param("maxPrice", testRoom.getPriceDay().add(BigDecimal.valueOf(10000)).toString())
					.with(jwt().jwt(builder ->
							builder.claim("sub", testHotel.getOwner().toString())))
			).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SearchRoomsDto.class);

		Assertions.assertEquals(1, searchRoomsDto.rooms().size());
		Assertions.assertTrue(searchRoomsDto.rooms().stream().anyMatch(room ->
				(room.availableRooms() - testRoom.getCountRoom() == 0) &&
						room.id().equals(testRoom.getId())));
	}


	/*
	Удаления медиа отсрочивается на срок работы обработчика, проверим удаления медиа как поиск null значений в таблице RoomMedia.
	(Media deletion is delayed for the duration of the handler, let's check media deletion as a search for null values in the RoomMedia table.)
	 */
	@Test
	@DisplayName("ПРОВЕРКА DELETE /api/v1/room/{id}, param -> keyMedia")
	@Transactional
	void testDeleteMedia() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
				.delete("/api/v1/room/" + testRoom.getId())
				.param("keyMedia", testRoom.getMedia().getFirst().getUrl())
				.with(jwt().jwt(builder ->
						builder.claim("sub", testHotel.getOwner().toString())))

		).andExpect(status().isOk());

        Assertions.assertEquals(0, roomMediaRepository.countMediaByRoom(testRoom.getId()));
		Assertions.assertTrue(roomMediaRepository.deleteByRoomIsNull() > 0);
	}

}
