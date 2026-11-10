package airplane;

import airplane.components.Engine;
import airplane.components.FuelSystem;
import airplane.components.LandingGear;
import airplane.components.NavigationSystem;
import airplane.exceptions.FlightException;
import airplane.models.FlightStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AircraftTest {

    private Engine mockEngine;
    private FuelSystem mockFuelSystem;
    private LandingGear mockLandingGear;
    private NavigationSystem mockNavigationSystem;

    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        mockEngine = mock(Engine.class);
        mockFuelSystem = mock(FuelSystem.class);
        mockLandingGear = mock(LandingGear.class);
        mockNavigationSystem = mock(NavigationSystem.class);

        aircraft = new Aircraft(mockEngine, mockFuelSystem, mockLandingGear, mockNavigationSystem);
    }

    @Test
    void prepareForTakeoff_AllSystemsGo_ShouldChangeStatusToReady() {
        when(mockEngine.isOperational()).thenReturn(true);
        when(mockFuelSystem.hasEnoughFuel()).thenReturn(true);
        when(mockNavigationSystem.isRouteAvailable()).thenReturn(true);

        aircraft.prepareForTakeoff();

        assertEquals(FlightStatus.READY, aircraft.getStatus());
        verify(mockEngine, times(1)).isOperational();
    }

    @Test
    void prepareForTakeoff_EngineSensorFails_ShouldThrowException() {
        when(mockEngine.isOperational()).thenThrow(new RuntimeException("Engine sensor connection lost"));
        assertThrows(RuntimeException.class, () -> aircraft.prepareForTakeoff());
    }

    @Test
    void takeOff_WhenReady_ShouldChangeStatusToInFlightAndUseComponents() {
        when(mockEngine.isOperational()).thenReturn(true);
        when(mockFuelSystem.hasEnoughFuel()).thenReturn(true);
        when(mockNavigationSystem.isRouteAvailable()).thenReturn(true);
        aircraft.prepareForTakeoff();

        doNothing().when(mockEngine).start();
        doNothing().when(mockLandingGear).retract();

        aircraft.takeOff();

        assertEquals(FlightStatus.IN_FLIGHT, aircraft.getStatus());
        verify(mockEngine).start();
        verify(mockLandingGear).retract();
    }

    @Test
    void takeOff_EngineFailsToStart_ShouldAbortFlight() {
        when(mockEngine.isOperational()).thenReturn(true);
        when(mockFuelSystem.hasEnoughFuel()).thenReturn(true);
        when(mockNavigationSystem.isRouteAvailable()).thenReturn(true);
        aircraft.prepareForTakeoff();

        doThrow(new IllegalStateException("Engine failed")).when(mockEngine).start();

        assertThrows(FlightException.class, () -> aircraft.takeOff());
        assertEquals(FlightStatus.ABORTED, aircraft.getStatus());
    }

    @Test
    void cruise_WithSufficientFuel_ShouldConsumeFuel() {
        when(mockEngine.isOperational()).thenReturn(true);
        when(mockFuelSystem.hasEnoughFuel()).thenReturn(true);
        when(mockNavigationSystem.isRouteAvailable()).thenReturn(true);
        aircraft.prepareForTakeoff();
        aircraft.takeOff();

        aircraft.cruise(100.0);

        verify(mockFuelSystem).consumeFuel(any(Double.class));
    }

    @Test
    void reset_ShouldClearMockInteractions() {
        when(mockEngine.isOperational()).thenReturn(true);
        when(mockFuelSystem.hasEnoughFuel()).thenReturn(true);
        when(mockNavigationSystem.isRouteAvailable()).thenReturn(true);

        aircraft.prepareForTakeoff();

        reset(mockEngine);

        verifyNoInteractions(mockEngine);
    }
}