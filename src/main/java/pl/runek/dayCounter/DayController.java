package pl.runek.dayCounter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class DayController {
    @GetMapping("/")
    String mainTemplate(Model model) {
        model.addAttribute("dateFrom", LocalDate.now());
        model.addAttribute("dateTo", LocalDate.now());
        model.addAttribute("days", 0);
        model.addAttribute("weekdays", 0);
        return "index";
    }

    @GetMapping("/dateChange")
    String from(@RequestParam LocalDate dateFrom,
                @RequestParam LocalDate dateTo,
                @RequestParam Long days,
                @RequestParam Long weekdays,
                Model model) {
        days = dateFrom.until(dateTo, ChronoUnit.DAYS);
        weekdays = calculateWeekdays(dateFrom, dateTo);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        return "inputs";
    }

    @GetMapping("/days")
    String days(@RequestParam LocalDate dateFrom,
                @RequestParam LocalDate dateTo,
                @RequestParam Long days,
                @RequestParam Long weekdays,
                Model model) {
        dateTo = dateFrom.plusDays(days);
        weekdays = calculateWeekdays(dateFrom, dateTo);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        return "inputs";
    }

    @GetMapping("/weekdays")
    String weekdays(@RequestParam LocalDate dateFrom,
                @RequestParam LocalDate dateTo,
                @RequestParam Long days,
                @RequestParam Long weekdays,
                Model model) {
        dateTo = calculateDateTo(dateFrom, weekdays);
        days = dateFrom.until(dateTo, ChronoUnit.DAYS);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("days", days);
        model.addAttribute("weekdays", weekdays);
        return "inputs";
    }

    private Long calculateWeekdays(LocalDate dateFrom, LocalDate dateTo) {
        if(dateFrom.isAfter(dateTo)) {
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

    private LocalDate calculateDateTo(LocalDate dateFrom, Long weekdays) {
        long weeks = weekdays/5;

        LocalDate output = dateFrom.plusDays(0);
        while (output.getDayOfWeek().equals(DayOfWeek.SATURDAY) || output.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            output = output.plusDays(1);
        }
        output = output.plusDays(weekdays%5);
        output = output.plusDays(weeks * 7);
        return output;
    }
}
