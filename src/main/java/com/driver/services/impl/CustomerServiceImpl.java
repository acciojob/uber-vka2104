package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer> customerOpt = customerRepository2.findById(customerId);
        customerOpt.ifPresent(customer -> customerRepository2.delete(customer));
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		//get customer by id
		Optional<Customer> customerOpt = customerRepository2.findById(customerId);
		if(!customerOpt.isPresent()) return null;
		Customer customer = customerOpt.get();

		//get all drivers based on id sorted order and get the available driver
		Sort sort = Sort.by(Sort.Direction.ASC, "driverId");
		List<Driver> drivers = driverRepository2.findAll(sort);
//		if(drivers == null || drivers.size() == 0) {
//            throw new Exception("No cab available!");
//        }
		Driver availableDriver = null;
		for(Driver driver: drivers) {
			if(driver.getCab().getAvailable()) {
				availableDriver = driver;
				break;
			}
		}
		if(availableDriver == null) throw new Exception("No cab available!");
		//create instance for trip booking
		TripBooking tripBooking = new TripBooking();
		//sewt the details to trip booking object
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(availableDriver);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setBill(distanceInKm*availableDriver.getCab().getPerKmRate());
        //save the trip
		TripBooking savedTripBooking = tripBookingRepository2.save(tripBooking);
		//update the customer with tripbooking
		customer.getTripBookingList().add(savedTripBooking);
		Customer updatedCustomer = customerRepository2.save(customer);
		//set the can available status to false and update the tripbooking to the driver
		availableDriver.getCab().setAvailable(false);
		availableDriver.getTripBookingList().add(savedTripBooking);
		Driver updatedDriver = driverRepository2.save(availableDriver);

		return savedTripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		try {
			//Cancel the trip having given trip Id and update TripBooking attributes accordingly
			Optional<TripBooking> tripBookingOpt = tripBookingRepository2.findById(tripId);
			if(tripBookingOpt.isPresent()) {
				TripBooking tripBooking = tripBookingOpt.get();
				tripBooking.setStatus(TripStatus.CANCELED);
				tripBooking.setBill(0);
				TripBooking updatedTripBooking = tripBookingRepository2.save(tripBooking);

				Driver driver = updatedTripBooking.getDriver();
				if(driver != null) {
					if(driver.getCab() != null) {
						driver.getCab().setAvailable(true);
						Driver updatedDriver = driverRepository2.save(driver);
					}
				}
			}
		} catch (NullPointerException ignored) {

		}


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBookingOpt = tripBookingRepository2.findById(tripId);
		if(tripBookingOpt.isPresent()) {
			TripBooking tripBooking = tripBookingOpt.get();
			tripBooking.setStatus(TripStatus.COMPLETED);
			TripBooking updatedTripBooking  = tripBookingRepository2.save(tripBooking);

//			Customer customer = updatedTripBooking.getCustomer();
//			for (TripBooking trip: customer.getTripBookingList()) {
//				if(trip.getTripBookingId() == tripId) {
//					trip.setStatus(TripStatus.COMPLETED);
//				}
//			}
//			Customer updatedCustomer = customerRepository2.save(customer);

			Driver driver = updatedTripBooking.getDriver();
			driver.getCab().setAvailable(true);
//			for (TripBooking trip: driver.getTripBookingList()) {
//				if(trip.getTripBookingId() == tripId) {
//					trip.setStatus(TripStatus.COMPLETED);
//				}
//			}

			Driver updatedDriver = driverRepository2.save(driver);
		}
	}
}
