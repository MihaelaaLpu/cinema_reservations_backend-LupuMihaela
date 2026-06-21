package com.uni.backend.service;

import com.uni.backend.entity.*;
import com.uni.backend.exception.ResourceNotFoundException;
import com.uni.backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {
    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket testTicket;
    private Screening testScreening;
    private CinemaRoom testRoom;
    private User testUser;

    @BeforeEach
    void setUp() {
        // setup user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");

        // setup cinema room
        testRoom = new CinemaRoom();
        testRoom.setId(1L);
        testRoom.setCapacity(50);

        // setup screening
        testScreening = new Screening();
        testScreening.setId(1L);
        testScreening.setCinemaRoom(testRoom);

        // setup ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setScreening(testScreening);
        testTicket.setTicketType("ADULT");
        testTicket.setSeatRow("A");
        testTicket.setSeatNumber(12);
        testTicket.setUser(testUser);
    }

    @AfterEach
    void tearDown() {
        // clear the security context after each test to prevent test pollution
        SecurityContextHolder.clearContext();
    }

    // helper method to mock the Spring Security Context
    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void buyTicket_ShouldSaveTicketWithAdultPrice_WhenRoomNotFull() {
        // arrange
        mockSecurityContext("johndoe");

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(ticketRepository.getTicketsByScreeningId(1L)).thenReturn(new ArrayList<>()); // 0 sold tickets
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // act
        Ticket savedTicket = ticketService.buyTicket(testTicket);

        // assert
        assertNotNull(savedTicket);
        assertEquals(25.0f, testTicket.getPrice(), 0.01f); // check base price is applied
        assertNotNull(testTicket.getPurchaseDate());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void buyTicket_ShouldSaveTicketWithChildPrice_WhenTypeIsChild() {
        // arrange
        mockSecurityContext("johndoe");
        testTicket.setTicketType("CHILD");

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(ticketRepository.getTicketsByScreeningId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // act
        ticketService.buyTicket(testTicket);

        // assert
        assertEquals(12.5f, testTicket.getPrice(), 0.01f); // 50% of 25
    }

    @Test
    void buyTicket_ShouldSaveTicketWithSeniorPrice_WhenTypeIsSenior() {
        // arrange
        mockSecurityContext("johndoe");
        testTicket.setTicketType("SENIOR");

        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(ticketRepository.getTicketsByScreeningId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // act
        ticketService.buyTicket(testTicket);

        // assert
        assertEquals(17.5f, testTicket.getPrice(), 0.01f); // 30% off 25
    }

    @Test
    void buyTicket_ShouldThrowException_WhenRoomIsFull() {
        // arrange
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        // mock that 50 tickets are already sold for this screening
        List<Ticket> soldTickets = Collections.nCopies(50, new Ticket());
        when(ticketRepository.getTicketsByScreeningId(1L)).thenReturn(soldTickets);

        // act & assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.buyTicket(testTicket));

        assertEquals("We are sorry but the cinema room is full!", exception.getMessage());
        verify(userRepository, never()).findByUsername(anyString());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void getAllTickets_ShouldReturnAllTickets() {
        when(ticketRepository.findAll()).thenReturn(List.of(testTicket));

        List<Ticket> result = ticketService.getAllTickets();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getTicketById_ShouldReturnTicket_WhenExists() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        Ticket result = ticketService.getTicketById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getAllTicketsByScreeningId_ShouldReturnTickets_WhenScreeningExists() {
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));
        when(ticketRepository.getTicketsByScreeningId(1L)).thenReturn(List.of(testTicket));

        List<Ticket> result = ticketService.getAllTicketsByScreeningId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateTicketById_ShouldRecalculatePriceAndSave_WhenExists() {
        // arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(testScreening));

        Ticket updateRequest = new Ticket();
        updateRequest.setScreening(testScreening);
        updateRequest.setSeatRow("B");
        updateRequest.setSeatNumber(15);
        updateRequest.setTicketType("CHILD"); // changing type to see if price recalculates

        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // act
        Ticket updatedTicket = ticketService.updateTicketById(updateRequest, 1L);

        // assert
        assertNotNull(updatedTicket);
        assertEquals("B", updatedTicket.getSeatRow());
        assertEquals(15, updatedTicket.getSeatNumber());
        assertEquals(12.5f, updatedTicket.getPrice(), 0.01f); // verify price was updated

        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void updateTicketById_ShouldThrowException_WhenTicketNotFound() {
        when(ticketRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.updateTicketById(testTicket, 2L));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void deleteTicketById_ShouldDelete_WhenExists() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        ticketService.deleteTicketById(1L);

        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).delete(testTicket);
    }

    @Test
    void deleteTicketById_ShouldThrowException_WhenNotExists() {
        when(ticketRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.deleteTicketById(2L));
        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void findPaginated_ShouldReturnTicketPage() {
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));
        when(ticketRepository.findAll(any(Pageable.class))).thenReturn(ticketPage);

        Page<Ticket> result = ticketService.findPaginated(1, 10, "seatRow", "ASC");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(ticketRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void findPaginatedForUser_ShouldReturnTicketPageForSpecificUser() {
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));
        when(ticketRepository.findByUserUsername(eq("johndoe"), any(Pageable.class))).thenReturn(ticketPage);

        Page<Ticket> result = ticketService.findPaginatedForUser(1, 10, "seatRow", "DESC", "johndoe");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(ticketRepository, times(1)).findByUserUsername(eq("johndoe"), any(Pageable.class));
    }
}
