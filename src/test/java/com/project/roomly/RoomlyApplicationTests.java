package com.project.roomly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.roomly.dto.Booking.BookingDto;
import com.project.roomly.dto.Booking.ResponseBookingDto;
import com.project.roomly.dto.Booking.ResponseBookingsDto;
import com.project.roomly.dto.Hotel.RequestHotelDto;
import com.project.roomly.dto.Hotel.SetHotelDto;
import com.project.roomly.dto.Media.*;
import com.project.roomly.dto.Room.ResponseRoomDto;
import com.project.roomly.dto.Room.RoomDto;
import com.project.roomly.dto.Room.SearchRoomsDto;
import com.project.roomly.dto.Room.SetRoomDto;
import com.project.roomly.entity.*;
import com.project.roomly.repository.*;
import com.project.roomly.scheduler.MediaScheduler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
@Transactional
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

	@Autowired
	private BookingRepository bookingRepository;

	@MockitoBean
	private S3Client s3Client;

	@MockitoBean
	private MediaScheduler mediaScheduler;

	@Value("${pageable.size}")
	private Integer pageableSize;


	private Hotel testHotel;

	private Room testRoom;

	private Booking testBooking;

	@BeforeEach
	void setupHotel() {
		testHotel = new Hotel();
		testHotel.setName("Test Hotel");
		testHotel.setAddress("Test Address");
		testHotel.setOwner(UUID.randomUUID());
		testHotel.setPrepaymentPercentage(10);
		testHotel.setCity("Москва");
		HotelMedia hotelMedia = new HotelMedia("https://test/image.png", testHotel);
		testHotel.setMedia(List.of(hotelMedia));
		hotelMediaRepository.save(hotelMedia);
		testHotel = hotelRepository.save(testHotel);


		testRoom = new Room();
		testRoom.setHotel(testHotel);
		testRoom.setCountRoom(10);
		testRoom.setPriceDay(BigDecimal.valueOf(1000));
		testRoom.setName("Номер тест");
		testRoom.setDescription("Описание номера");
		testRoom.setFloor(1);
		RoomMedia roomMedia = new RoomMedia("image.png", testRoom);
		testRoom.setMedia(List.of(roomMedia));
		testRoom = roomRepository.save(testRoom);
		roomMediaRepository.save(roomMedia);

		testBooking = new Booking();
		testBooking.setCreateTime(LocalDateTime.now());
		testBooking.setUserId(UUID.randomUUID());
		testBooking.setStartTime(LocalDate.now().plusDays(10));
		testBooking.setEndTime(LocalDate.now().plusDays(20));
		testBooking.setRoom(testRoom);
		testBooking.setStatusBooking(StatusBooking.AWAIT_PAY);
		testBooking.setPrice(testRoom.getPriceDay().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(testBooking.getStartTime(), testBooking.getEndTime()))));
		testBooking.setPrepayment(testBooking.getPrice().multiply(BigDecimal.valueOf(testHotel.getPrepaymentPercentage())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
		testBooking = bookingRepository.save(testBooking);
	}



	// --------------- TEST ROOM ---------------
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
        Assertions.assertEquals(0, setRoom.getPriceDay().compareTo(setRoomDto.priceDay()));
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

		List<String> mediaKeys = roomMediaRepository.findMediaByRoomId(testRoom.getId());

		Assertions.assertEquals(responseRoomMediaDto.room().countRoom(), testRoom.getCountRoom());
		Assertions.assertEquals(0, responseRoomMediaDto.room().priceDay().compareTo(testRoom.getPriceDay()));
		Assertions.assertEquals(responseRoomMediaDto.room().prepaymentPercentage(), testRoom.getHotel().getPrepaymentPercentage());
		Assertions.assertEquals(responseRoomMediaDto.room().name(), testRoom.getName());
		Assertions.assertEquals(responseRoomMediaDto.room().description(), testRoom.getDescription());
		Assertions.assertEquals(responseRoomMediaDto.room().floor(), testRoom.getFloor());
		Assertions.assertEquals(responseRoomMediaDto.room().hotelId(), testRoom.getHotel().getId());
		Assertions.assertEquals(responseRoomMediaDto.media().size(), roomMediaRepository.countMediaByRoom(testRoom.getId()));
		Assertions.assertTrue(responseRoomMediaDto.media().stream().allMatch(media -> mediaKeys.contains(media.url())));
	}

	@Test
	@DisplayName("ПРОВЕРКА POST /api/v1/room/{id}")
	void testAddMedia() throws Exception {
		when(s3Client.putObject( any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
		MockMultipartFile mediaPart1 = new MockMultipartFile(
				"media",
				"testMedia",
				"video/mp4",
				"another test media".getBytes()
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
		Assertions.assertTrue(mediaKeys.stream().anyMatch(url -> url.contains(".mp4")));
	}

	/*
	Удаления медиа отсрочивается на срок работы обработчика, проверим удаления медиа как поиск null значений в таблице RoomMedia.
	(Media deletion is delayed for the duration of the handler, let's check media deletion as a search for null values in the RoomMedia table.)
	 */
	@Test
	@DisplayName("ПРОВЕРКА DELETE /api/v1/room/{id}")
	void testDeleteRoom() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
				.delete("/api/v1/room/" + testRoom.getId())
				.with(jwt().jwt(builder ->
						builder.claim("sub", testHotel.getOwner().toString())))

		).andExpect(status().isOk());
		Assertions.assertTrue(roomRepository.findById(testRoom.getId()).isPresent());
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
					.param("startDate", LocalDate.now().toString())
					.param("endDate", LocalDate.now().plusDays(1).toString())
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


	// --------------- TEST HOTEL  ---------------

	@Test
	@DisplayName("Проверка GET /api/v1/hotel")
	void testGetHotelsOwner() throws Exception {

		List<String> keyMedia = hotelMediaRepository.findMediaKeyByHotelId(testHotel.getId());

		ResponseHotelsMediaDto responseHotelsMediaDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders
				.get("/api/v1/hotel")
				.param("page", "0")
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString())))
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ResponseHotelsMediaDto.class);

		Assertions.assertTrue(responseHotelsMediaDto.hotels().size() >= 1);
		Assertions.assertTrue(responseHotelsMediaDto.hotels().stream().anyMatch( responseHotelMediaDto ->
						testHotel.getName().equals(responseHotelMediaDto.hotel().name()) &&
						testHotel.getId().equals(responseHotelMediaDto.hotel().id()) &&
						testHotel.getAddress().equals(responseHotelMediaDto.hotel().address()) &&
						responseHotelsMediaDto.hotels().stream().anyMatch( hotel -> hotel.media().stream().anyMatch( media -> keyMedia.contains(media.url())))
				)
		);
	}

	@Test
	@DisplayName("Проверка POST /api/v1/hotel")
	void testCreateHotel() throws Exception{
		when(s3Client.putObject( any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
		RequestHotelDto requestHotelDto = new RequestHotelDto("Отель", "Москва", "ул. Московская 1", 10);
		String hotelJson = objectMapper.writeValueAsString(requestHotelDto);

		MockMultipartFile roomPart = new MockMultipartFile(
				"hotel",
				"hotel.json",
				"application/json",
				hotelJson.getBytes()
		);

		MockMultipartFile mediaPart1 = new MockMultipartFile(
				"media",
				"image1.png",
				"image/png",
				"test image content".getBytes()
		);

		MockMultipartFile mediaPart2 = new MockMultipartFile(
				"media",
				"image1.png",
				"image/png",
				"test image content".getBytes()
		);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/hotel")
				.file(roomPart)
				.file(mediaPart1)
				.file(mediaPart2)
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString()))
		)).andExpect(status().isCreated());

		Assertions.assertTrue(hotelRepository.findAll().size() > 1);
	}


	@Test
	@DisplayName("Проверка PATCH /api/v1/hotel")
	void testUpdateHotel() throws Exception{

		SetHotelDto setHotelDto = new SetHotelDto(testHotel.getId(), "Рязань", "Лучший отель в Москве", "ул. Тверская 12", 12);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/hotel")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(setHotelDto))
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString()))
		)).andExpect(status().isOk());

		Optional<Hotel> setHotelTestOptional = hotelRepository.findById(testRoom.getId());
		Assertions.assertFalse(setHotelTestOptional.isEmpty());
		Hotel setHotel = setHotelTestOptional.get();
		Assertions.assertEquals(setHotel.getName(), setHotelDto.name());
		Assertions.assertEquals(setHotel.getAddress(), setHotelDto.address());
		Assertions.assertEquals(setHotel.getPrepaymentPercentage(), setHotelDto.prepaymentPercentage());
		Assertions.assertEquals(setHotel.getCity(), setHotelDto.city());
	}

	@Test
	@DisplayName("Проверка POST /api/v1/hotel/{id}")
	void testAddMediaHotel() throws Exception {


		MockMultipartFile mediaPart1 = new MockMultipartFile(
				"media",
				"testMedia",
				"video/mp4",
				"test media content".getBytes()
		);
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/hotel/" + testHotel.getId())
				.file(mediaPart1)
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString()))
		)).andExpect(status().isCreated());

		List<String> mediaKeys = hotelMediaRepository.findMediaKeyByHotelId(testHotel.getId());
		Assertions.assertFalse(mediaKeys.isEmpty());
		Assertions.assertTrue(mediaKeys.stream().anyMatch(url -> url.contains(".mp4")));
	}

	@Test
	@DisplayName("Проверка GET /api/v1/hotel/{id}")
	void testGetHotel() throws Exception{

		ResponseHotelMediaDto responseHotelMediaDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/hotel/" + testHotel.getId())
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString()))
		)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ResponseHotelMediaDto.class);

		List<String> mediaKeys = hotelMediaRepository.findMediaKeyByHotelId(testHotel.getId());
		Assertions.assertEquals(responseHotelMediaDto.hotel().name(), testHotel.getName());
		Assertions.assertEquals(responseHotelMediaDto.hotel().address(), testHotel.getAddress());
		Assertions.assertEquals(responseHotelMediaDto.media().size(), hotelMediaRepository.countMediaByHotel(testHotel.getId()));
		Assertions.assertTrue(responseHotelMediaDto.media().stream().allMatch(media -> mediaKeys.contains(media.url())));
	}

	/*
	Удаления медиа отсрочивается на срок работы обработчика, проверим удаления медиа как поиск null значений в таблице HotelMedia.
	(Media deletion is delayed for the duration of the handler, let's check media deletion as a search for null values in the HotelMedia table.)
	 */
	@Test
	@DisplayName("Проверка DELETE /api/hotel/{id}")
	void testDeleteHotel() throws Exception{

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/hotel/" + testHotel.getId())
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString())
		))).andExpect(status().isOk());


		Assertions.assertTrue(hotelRepository.findById(testHotel.getId()).isPresent());
		Assertions.assertTrue(roomRepository.findRoomsByHotelId(testHotel.getId(), Pageable.unpaged()).isEmpty());
		Assertions.assertTrue(hotelMediaRepository.deleteByHotelIsNull() > 0);
		Assertions.assertTrue(roomMediaRepository.deleteByRoomIsNull() > 0);
	}

	@Test
	@DisplayName("Проверка GET /api/hotel/rooms, param -> hotelId, page")
	void testGetRoomsByHotelId() throws Exception{

		ResponseRoomsMediaDto responseRoomsMediaDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/hotel/rooms")
				.param("hotelId", testHotel.getId().toString())
				.param("page", "0")
				.with(jwt().jwt(builder -> builder.claim("sub", testHotel.getOwner().toString())
		))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ResponseRoomsMediaDto.class);


		List<ResponseRoomMediaDto> responseRoomMediaDtoList = responseRoomsMediaDto.rooms();

		List<ResponseRoomDto> roomList = roomRepository.findRoomsByHotelId(testHotel.getId(), PageRequest.of(0, pageableSize));
		List<ResponseMediaDto> mediaList = roomMediaRepository.findMediasByRoomsId(roomList.stream().map(ResponseRoomDto::id).toList());
		List<String> keyMedias = mediaList.stream().map(ResponseMediaDto::url).toList();

		Assertions.assertFalse(responseRoomMediaDtoList.isEmpty());

		keyMedias.forEach(k -> System.out.println("key: '" + k + "'"));

		Assertions.assertTrue(responseRoomMediaDtoList.stream().anyMatch(room -> {
			ResponseRoomDto responseRoomDto = room.room();
					return responseRoomDto.description().equals(testRoom.getDescription()) &&
							responseRoomDto.countRoom().equals(testRoom.getCountRoom()) &&
							responseRoomDto.floor().equals(testRoom.getFloor()) &&
							responseRoomDto.name().equals(testRoom.getName()) &&
							responseRoomDto.id().equals(testRoom.getId()) &&
							responseRoomDto.hotelId().equals(testHotel.getId()) &&
							room.media().stream().allMatch(media -> keyMedias.contains(media.url())) &&
							responseRoomDto.priceDay().compareTo(testRoom.getPriceDay()) == 0 &&
							responseRoomDto.prepaymentPercentage().compareTo(testHotel.getPrepaymentPercentage()) == 0;
				}));
	}

	/*
	Удаления медиа отсрочивается на срок работы обработчика, проверим удаления медиа как поиск null значений в таблице HotelMedia.
	(Media deletion is delayed for the duration of the handler, let's check media deletion as a search for null values in the HotelMedia table.)
	 */
	@Test
	@DisplayName("Проверка DELETE /api/v1/hotel/{id}")
	void testDeleteMediaHotel() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders
				.delete("/api/v1/hotel/" + testHotel.getId())
				.param("keyMedia", testHotel.getMedia().getFirst().getUrl())
				.with(jwt().jwt(builder ->
						builder.claim("sub", testHotel.getOwner().toString())))
		).andExpect(status().isOk());

		Assertions.assertEquals(0, hotelMediaRepository.countMediaByHotel(testHotel.getId()));
		Assertions.assertTrue(hotelMediaRepository.deleteByHotelIsNull() > 0);
	}



	// --------------- TEST BOOKING  ---------------
	@Test
	@DisplayName("Проверка GET /api/v1/booking")
	void testGetBookings() throws Exception{

		String response = mockMvc.perform(MockMvcRequestBuilders
				.get("/api/v1/booking")
				.param("page", "0")
				.with(jwt().jwt(builder ->
						builder.claim("sub", testBooking.getUserId().toString())))
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();


		ResponseBookingsDto responseBookingsDto = objectMapper.readValue(response, ResponseBookingsDto.class);

        Assertions.assertFalse(responseBookingsDto.bookings().isEmpty());
		Assertions.assertTrue(responseBookingsDto.bookings().stream().anyMatch(booking ->
				testBooking.getStatusBooking().equals(booking.statusBooking()) &&
				testBooking.getPrice().compareTo(booking.price()) == 0 &&
				testBooking.getPrepayment().compareTo(booking.prepayment()) == 0 &&
				testBooking.getStartTime().equals(booking.startTime()) &&
				testBooking.getEndTime().equals(booking.endTime())
				));
	}

	@Test
	@DisplayName("Проверка POST /api/v1/booking")
	void testCreateBooking() throws  Exception{

		BookingDto bookingDto = new BookingDto(testRoom.getId(), LocalDate.now(), LocalDate.now().plusDays(1), testRoom.getPriceDay(), testHotel.getPrepaymentPercentage());

		ResponseBookingDto  responseBookingDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/booking")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bookingDto))
				.with(jwt().jwt(builder ->
						builder.claim("sub", testBooking.getUserId().toString())))
		).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), ResponseBookingDto.class);

		Optional<Booking> optionalBooking = bookingRepository.findById(responseBookingDto.bookingId());
		Assertions.assertTrue(optionalBooking.isPresent());
		Booking booking = optionalBooking.get();
        Assertions.assertEquals(responseBookingDto.statusBooking(), booking.getStatusBooking());
		Assertions.assertEquals(responseBookingDto.endTime(), booking.getEndTime());
		Assertions.assertEquals(responseBookingDto.startTime(), booking.getStartTime());
		Assertions.assertEquals(responseBookingDto.prepayment(), booking.getPrepayment());
		Assertions.assertEquals(responseBookingDto.price(), booking.getPrice());
		Assertions.assertEquals(responseBookingDto.roomId(), booking.getRoom().getId());
	}

	@Test
	@DisplayName("Проверка GET /api/v1/booking/{id}")
	void testGetBooking() throws  Exception{

		ResponseBookingDto responseBookingDto = objectMapper.readValue(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/booking/" + testBooking.getId())
				.with(jwt().jwt(builder ->
						builder.claim("sub", testBooking.getUserId().toString())))
		).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ResponseBookingDto.class);

		Optional<Booking> optionalBooking = bookingRepository.findById(responseBookingDto.bookingId());
		Assertions.assertTrue(optionalBooking.isPresent());
		Booking booking = optionalBooking.get();
		Assertions.assertEquals(responseBookingDto.statusBooking(), booking.getStatusBooking());
		Assertions.assertEquals(responseBookingDto.endTime(), booking.getEndTime());
		Assertions.assertEquals(responseBookingDto.startTime(), booking.getStartTime());
		Assertions.assertEquals(responseBookingDto.prepayment(), booking.getPrepayment());
		Assertions.assertEquals(responseBookingDto.price().compareTo(booking.getPrice()), 0);
		Assertions.assertEquals(responseBookingDto.roomId(), booking.getRoom().getId());

	}
}
