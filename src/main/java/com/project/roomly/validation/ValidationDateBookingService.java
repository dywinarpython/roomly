package com.project.roomly.validation;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ValidationDateBookingService {

    public void checkDate(LocalDate start, LocalDate end){
        if(start.isBefore(LocalDate.now())){
            throw new ValidationException("Дата начала не может быть ранее сегодняшнего дня (The start date cannot be earlier than today)");
        }
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания (The start date cannot be later than the end date)");
        }
        if (start.isEqual(end)) {
            throw new ValidationException("Дата начала и окончания не могут быть одинаковыми (The start and end dates cannot be the same.)");
        }
    }
}
