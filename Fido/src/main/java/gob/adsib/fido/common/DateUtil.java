package gob.adsib.fido.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * La Paz - Bolivia
 * EDV
 * Created by: equinajo
 */
public class DateUtil {

    public static final String FORMAT_DATE = "dd/MM/yyyy";
    public static final String FORMAT_DATE_MES = "ddMMMyyyy";
    public static final String FORMAT_DATE_TIME = "dd/MM/yyyy HH:mm";
    public static final String FORMAT_DATE_TIME_SECOND = "dd/MM/yyyy HH:mm:ss";
    public static final String FORMAT_TIME = "HH:mm:ss";
    public static final String FORMAT_HOUR_SECOND = "HH:mm";
    public static final String FORMAT_KEY = "yyyyMMddHHmmssSSS";

    public static final String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    public static String toString(String format, Date fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(fecha);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toStringLiteral(Date fecha) {
        if (fecha == null) {
            return "null";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        StringBuilder sb = new StringBuilder("");
        sb.append(calendar.get(Calendar.DAY_OF_MONTH));
        sb.append(" de ").append(meses[calendar.get(Calendar.MONTH)]);
        sb.append(" del ").append(calendar.get(Calendar.YEAR));
        return sb.toString();
    }

    public static String toStringLiteralReport(Date fecha) {
        if (fecha == null) {
            return "null";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        StringBuilder sb = new StringBuilder("");
        sb.append(" Al ");
        sb.append(calendar.get(Calendar.DAY_OF_MONTH));
        sb.append(" de ").append(meses[calendar.get(Calendar.MONTH)]);
        sb.append(" del ").append(calendar.get(Calendar.YEAR));
        return sb.toString();
    }

    public static Date cambiarPrimeraHora(Date fecha) {
        if (fecha == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date cambiarUltimaHora(Date fecha) {
        if (fecha == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static Date cambiarFechaActual(Date fecha) {
        if (fecha == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        Calendar fechaActual = (Calendar) calendar.clone();
        calendar.setTime(fecha);
        calendar.set(Calendar.DAY_OF_MONTH, fechaActual.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH, fechaActual.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, fechaActual.get(Calendar.YEAR));
        return calendar.getTime();
    }

    public static Date obtenerPrimeraFechaSemana() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date obtenerUltimaFechaSemana() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static Date obtenerPrimeraFechaMes() {
        Calendar calendar = Calendar.getInstance();
        Calendar inicioMesActual = (Calendar) calendar.clone();
        inicioMesActual.set(Calendar.DAY_OF_MONTH, inicioMesActual.getActualMinimum(Calendar.DAY_OF_MONTH));
        inicioMesActual.set(Calendar.HOUR_OF_DAY, 0);
        inicioMesActual.set(Calendar.MINUTE, 0);
        inicioMesActual.set(Calendar.SECOND, 0);
        inicioMesActual.set(Calendar.MILLISECOND, 0);
        return inicioMesActual.getTime();
    }

    public static Date obtenerUltimaFechaMes() {
        Calendar calendar = Calendar.getInstance();
        Calendar inicioMesActual = (Calendar) calendar.clone();
        inicioMesActual.set(Calendar.DAY_OF_MONTH, inicioMesActual.getActualMinimum(Calendar.DAY_OF_MONTH));
        Calendar finMesActual = (Calendar) calendar.clone();
        finMesActual.set(Calendar.DAY_OF_MONTH, inicioMesActual.getActualMaximum(Calendar.DAY_OF_MONTH));
        finMesActual.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        finMesActual.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        finMesActual.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        finMesActual.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return finMesActual.getTime();
    }

    public static Date obtenerPrimeraFechaMes(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        Calendar inicioMesActual = (Calendar) calendar.clone();
        inicioMesActual.set(Calendar.DAY_OF_MONTH, inicioMesActual.getActualMinimum(Calendar.DAY_OF_MONTH));
        inicioMesActual.set(Calendar.HOUR_OF_DAY, 0);
        inicioMesActual.set(Calendar.MINUTE, 0);
        inicioMesActual.set(Calendar.SECOND, 0);
        inicioMesActual.set(Calendar.MILLISECOND, 0);
        return inicioMesActual.getTime();
    }

    public static Date obtenerUltimaFechaMes(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        Calendar inicioMesActual = (Calendar) calendar.clone();
        inicioMesActual.set(Calendar.DAY_OF_MONTH, inicioMesActual.getActualMinimum(Calendar.DAY_OF_MONTH));
        Calendar finMesActual = (Calendar) calendar.clone();
        finMesActual.set(Calendar.DAY_OF_MONTH, inicioMesActual.getActualMaximum(Calendar.DAY_OF_MONTH));
        finMesActual.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        finMesActual.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        finMesActual.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        finMesActual.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return finMesActual.getTime();
    }

    public static Date obtenerPrimerDiaAnio() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.getActualMinimum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date obtenerUltimoDiaAnio() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static Date obtenerPrimerDiaAnio(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.MONTH, calendar.getActualMinimum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static int obtenerDiasDiferenciaEntreDosFechas(Date fechaIni, Date fechaFin) {
        int dias = (int) ((fechaIni.getTime() - fechaFin.getTime()) / 86400000);
        return dias;
    }

    public static int obtenerMesesDiferenciaEntreDosFechas(Date fechaIni, Date fechaFin) {
        Calendar inicio = new GregorianCalendar();
        Calendar fin = new GregorianCalendar();
        inicio.setTime(fechaIni);
        fin.setTime(fechaFin);
        int difA = fin.get(Calendar.YEAR) - inicio.get(Calendar.YEAR);
        int difM = difA * 12 + fin.get(Calendar.MONTH) - inicio.get(Calendar.MONTH);
        return difM;
    }


    public static Date obtenerUltimoDiaAnio(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static long workingDaysBetweenTwoDates(Date startDate, Date endDate) {
        if (startDate == null) {
//            System.out.println("fechaInicio: "+startDate);
            return 0;
        }
        if (endDate == null) {
//            System.out.println("fechaFin: "+endDate);
            return 0;
        }
        startDate = cambiarPrimeraHora(startDate);
        endDate = cambiarPrimeraHora(endDate);
        //Calculando dias laborales
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        //
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        int workDays = 0;
        //Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return 0;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }

        do {
            //excluding start date
            startCal.add(Calendar.DAY_OF_MONTH, 1);
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                ++workDays;
            }
        } while (startCal.getTimeInMillis() < endCal.getTimeInMillis()); //excluding end date

        return workDays;
    }


    public static boolean between(Date fecha, Date fechaInicio, Date fechaFin) {
        boolean between = fechaInicio.compareTo(fecha) * fecha.compareTo(fechaFin) >= 0;
        return between;
    }

    public static Date sumarDias(Date fecha, Long dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.add(Calendar.DAY_OF_MONTH, dias.intValue());
        return calendar.getTime();
    }

    public static Date sumarMeses(Date fecha, Long meses) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.add(Calendar.MONTH, meses.intValue());
        return calendar.getTime();
    }

    public static Date sumarAnios(Date fecha, Long anios) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.add(Calendar.YEAR, anios.intValue());
        return calendar.getTime();
    }

    public static Date toDate(String format, String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(fecha);
        } catch (Exception e) {
            return null;
        }
    }

    public static String mesLabel(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        String mes = meses[calendar.get(Calendar.MONTH)];
        return mes;
    }

    public static String yearLabel(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        String year = calendar.get(Calendar.YEAR) + "";
        return year;
    }

    public static String capitalize(String source) {
        if (source==null || source.equals("")) {
            return "";
        }
        source = source.toLowerCase().trim();
        StringBuffer res = new StringBuffer();
        String[] strArr = source.split(" ");
        for (String str : strArr) {
            if(str.length()!=2 && str.length()!=1){
                char[] stringArray = str.trim().toCharArray();
                stringArray[0] = Character.toUpperCase(stringArray[0]);
                str = new String(stringArray);
            }
            res.append(str).append(" ");
        }
        return res.toString().trim();
    }

}
