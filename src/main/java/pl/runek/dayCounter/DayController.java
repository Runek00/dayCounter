package pl.runek.dayCounter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class DayController {

    @Autowired
    HolidayRepo holidayRepo;

    @GetMapping("/")
    String mainTemplate(Model model) {
        model.addAttribute("dateFrom", LocalDate.now());
        model.addAttribute("dateTo", LocalDate.now());
        model.addAttribute("days", 0);
        model.addAttribute("weekdays", 0);
        model.addAttribute("workdays", 0);
        return "index";
    }

    @GetMapping("/dateChange")
    String from(@RequestParam LocalDate dateFrom,
                @RequestParam LocalDate dateTo,
                @RequestParam Long days,
                @RequestParam Long weekdays,
                @RequestParam Long workdays,
                Model model) {
        days = dateFrom.until(dateTo, ChronoUnit.DAYS);
        weekdays = calculateWeekdays(dateFrom, dateTo);
        workdays = calculateWorkdays(dateFrom, dateTo, weekdays);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        model.addAttribute("workdays", workdays);
        return "inputs";
    }

    @GetMapping("/days")
    String days(@RequestParam LocalDate dateFrom,
                @RequestParam LocalDate dateTo,
                @RequestParam Long days,
                @RequestParam Long weekdays,
                @RequestParam Long workdays,
                Model model) {
        dateTo = dateFrom.plusDays(days);
        weekdays = calculateWeekdays(dateFrom, dateTo);
        workdays = calculateWorkdays(dateFrom, dateTo, weekdays);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        model.addAttribute("workdays", workdays);
        return "inputs";
    }

    @GetMapping("/weekdays")
    String weekdays(@RequestParam LocalDate dateFrom,
                    @RequestParam LocalDate dateTo,
                    @RequestParam Long days,
                    @RequestParam Long weekdays,
                    @RequestParam Long workdays,
                    Model model) {
        dateTo = calculateDateToFromWeekdays(dateFrom, weekdays);
        days = dateFrom.until(dateTo, ChronoUnit.DAYS);
        workdays = calculateWorkdays(dateFrom, dateTo, weekdays);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        model.addAttribute("workdays", workdays);
        return "inputs";
    }

    @GetMapping("/workdays")
    String workdays(@RequestParam LocalDate dateFrom,
                    @RequestParam LocalDate dateTo,
                    @RequestParam Long days,
                    @RequestParam Long weekdays,
                    @RequestParam Long workdays,
                    Model model) {
        dateTo = calculateDateToFromWorkdays(dateFrom, workdays);
        days = dateFrom.until(dateTo, ChronoUnit.DAYS);
        weekdays = calculateWeekdays(dateFrom, dateTo);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        model.addAttribute("workdays", workdays);
        return "inputs";
    }

    private Long calculateWeekdays(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom.isAfter(dateTo)) {
            return -calculateWeekdays(dateTo, dateFrom);
        }
        long weeks = dateFrom.until(dateTo, ChronoUnit.WEEKS);
        long extraDays = 0;
        LocalDate dateTemp = dateFrom.plusWeeks(weeks);
        while (dateTemp.isBefore(dateTo)) {
            dateTemp = dateTemp.plusDays(1);
            if (!dateTemp.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !dateTemp.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                extraDays++;
            }
        }
        return 5 * weeks + extraDays;
    }

    private Long calculateWorkdays(LocalDate dateFrom, LocalDate dateTo, long weekdays) {
        long holidays;
        if (dateFrom.isAfter(dateTo)) {
            holidays = -getHolidaysCount(dateTo, dateFrom);
        } else {
            holidays = getHolidaysCount(dateFrom, dateTo);
        }
        return weekdays - holidays;
    }

    private LocalDate calculateDateToFromWeekdays(LocalDate dateFrom, Long weekdays) {
        long weeks = weekdays / 5;

        LocalDate output = dateFrom.plusDays(0);
        for (int i = 0; i < weekdays % 5; i++) {
            output = output.plusDays(1);
            while (output.getDayOfWeek().equals(DayOfWeek.SATURDAY) || output.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                output = output.plusDays(1);
            }
        }
        output = output.plusDays(weeks * 7);
        return output;
    }

    private LocalDate calculateDateToFromWorkdays(LocalDate dateFrom, Long workdays) {
        LocalDate dateTo = calculateDateToFromWeekdays(dateFrom, workdays);
        long holidays = getHolidaysCount(dateFrom, dateTo);
        long holidaysSum = 0;
        while (holidays > 0) {
            holidaysSum += holidays;
            LocalDate dateTemp = dateTo.plusDays(1);
            dateTo = calculateDateToFromWeekdays(dateFrom, workdays + holidaysSum);
            holidays = getHolidaysCount(dateTemp, dateTo);
        }
        return dateTo;
    }

    private long getHolidaysCount(LocalDate dateFrom, LocalDate dateTo) {
        return holidayRepo.findAllBetweenDates(dateFrom, dateTo)
                .filter(holiday -> !holiday.theDay().getDayOfWeek().equals(DayOfWeek.SATURDAY) && !holiday.theDay().getDayOfWeek().equals(DayOfWeek.SUNDAY))
                .count();
    }
}
