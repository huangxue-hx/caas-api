package com.harmonycloud.common.util.date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);
    /**
     * 通用的一个DateFormat
     */
    public final static SimpleDateFormat commonDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 处理时分秒的DateFormat
     */
    public final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 不显示秒的DateFormat
     */
    public final static SimpleDateFormat noSecondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * utc时间格式
     */
    public final static SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final int ONE_HOUR_MILLISECONDS = 60*60*1000;
    public static final int ONE_MINUTE_MILLISECONDS = 60*1000;

    public final static String TIME_UNIT_SECOND = "s";
    public final static String TIME_UNIT_MINUTE = "m";
    public final static String TIME_UNIT_HOUR= "h";
    public final static String TIME_UNIT_DAY = "d";

    public static Date getCurrentUtcTime() {

        SimpleDateFormat adf = new SimpleDateFormat(DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());

        StringBuffer UTCTimeBuffer = new StringBuffer();
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day);
        UTCTimeBuffer.append("T").append(hour).append(":").append(minute).append(":").append(second).append("Z");
        Date date = null;
        try {
            date = adf.parse(UTCTimeBuffer.toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取SimpleDateFormat
     * 
     * @param parttern
     *            日期格式
     * @return SimpleDateFormat对象
     * @throws RuntimeException
     *             异常：非法日期格式
     */
    private static SimpleDateFormat getDateFormat(String parttern) throws RuntimeException{
        return new SimpleDateFormat(parttern);
    }

    /**
     * 获取日期中的某数值。如获取月份
     * 
     * @param date
     *            日期
     * @param dateType
     *            日期格式
     * @return 数值
     */
    private static int getInteger(Date date, int dateType){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(dateType);
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     * 
     * @param date
     *            日期字符串
     * @param dateType
     *            类型
     * @param amount
     *            数值
     * @return 计算后日期字符串
     */
    private static String addInteger(String date, int dateType, int amount){
        String dateString = null;
        DateStyle dateStyle = getDateStyle(date);
        if(dateStyle != null){
            Date myDate = StringToDate(date, dateStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = DateToString(myDate, dateStyle);
        }
        return dateString;
    }
    
    /**
     * 增加日期中某类型的某数值。如增加日期
     * 
     * @param date
     *            日期字符串
     * @param dateType
     *            类型
     * @param amount
     *            数值
     * @return 计算后日期字符串
     */
    private static String addInteger(String date, DateStyle dateStyle, int dateType, int amount){
        String dateString = null;
        if(dateStyle != null){
            Date myDate = StringToDate(date, dateStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = DateToString(myDate, dateStyle);
        }
        return dateString;
    }

    /**
     * 增加日期中某类型的某数值。如增加日期
     * 
     * @param date
     *            日期
     * @param dateType
     *            类型
     * @param amount
     *            数值
     * @return 计算后日期
     */
    private static Date addInteger(Date date, int dateType, int amount){
        Date myDate = null;
        if(date != null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(dateType, amount);
            myDate = calendar.getTime();
        }
        return myDate;
    }

    /**
     * 获取精确的日期
     * 
     * @param timestamps
     *            时间long集合
     * @return 日期
     */
    private static Date getAccurateDate(List<Long> timestamps){
        Date date = null;
        long timestamp = 0;
        Map<Long, long[]> map = new HashMap<Long, long[]>();
        List<Long> absoluteValues = new ArrayList<Long>();
        if(timestamps != null && timestamps.size() > 0){
            if(timestamps.size() > 1){
                for(int i = 0; i < timestamps.size(); i++){
                    for(int j = i + 1; j < timestamps.size(); j++){
                        long absoluteValue = Math.abs(timestamps.get(i) - timestamps.get(j));
                        absoluteValues.add(absoluteValue);
                        long[] timestampTmp = { timestamps.get(i), timestamps.get(j) };
                        map.put(absoluteValue, timestampTmp);
                    }
                }
                // 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的
                long minAbsoluteValue = -1;
                if(!absoluteValues.isEmpty()){
                    // 如果timestamps的size为2，这是差值只有一个，因此要给默认值
                    minAbsoluteValue = absoluteValues.get(0);
                }
                for(int i = 0; i < absoluteValues.size(); i++){
                    for(int j = i + 1; j < absoluteValues.size(); j++){
                        if(absoluteValues.get(i) > absoluteValues.get(j)){
                            minAbsoluteValue = absoluteValues.get(j);
                        }else{
                            minAbsoluteValue = absoluteValues.get(i);
                        }
                    }
                }
                if(minAbsoluteValue != -1){
                    long[] timestampsLastTmp = map.get(minAbsoluteValue);
                    if(absoluteValues.size() > 1){
                        timestamp = Math.max(timestampsLastTmp[0], timestampsLastTmp[1]);
                    }else if(absoluteValues.size() == 1){
                        // 当timestamps的size为2，需要与当前时间作为参照
                        long dateOne = timestampsLastTmp[0];
                        long dateTwo = timestampsLastTmp[1];
                        if((Math.abs(dateOne - dateTwo)) < 100000000000L){
                            timestamp = Math.max(timestampsLastTmp[0], timestampsLastTmp[1]);
                        }else{
                            long now = new Date().getTime();
                            if(Math.abs(dateOne - now) <= Math.abs(dateTwo - now)){
                                timestamp = dateOne;
                            }else{
                                timestamp = dateTwo;
                            }
                        }
                    }
                }
            }else{
                timestamp = timestamps.get(0);
            }
        }
        if(timestamp != 0){
            date = new Date(timestamp);
        }
        return date;
    }

    /**
     * 判断字符串是否为日期字符串
     * 
     * @param date
     *            日期字符串
     * @return true or false
     */
    public static boolean isDate(String date){
        boolean isDate = false;
        if(date != null){
            if(StringToDate(date) != null){
                isDate = true;
            }
        }
        return isDate;
    }

    /**
     * 获取日期字符串的日期风格。失敗返回null。
     * 
     * @param date
     *            日期字符串
     * @return 日期风格
     */
    public static DateStyle getDateStyle(String date){
        DateStyle dateStyle = null;
        Map<Long, DateStyle> map = new HashMap<Long, DateStyle>();
        List<Long> timestamps = new ArrayList<Long>();
        for(DateStyle style : DateStyle.values()){
            Date dateTmp = StringToDate(date, style.getValue());
            if(dateTmp != null){
                timestamps.add(dateTmp.getTime());
                map.put(dateTmp.getTime(), style);
            }
        }
        dateStyle = map.get(getAccurateDate(timestamps).getTime());
        return dateStyle;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @return 日期
     */
    public static Date StringToDate(String date){
        DateStyle dateStyle = null;
        return StringToDate(date, dateStyle);
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param parttern
     *            日期格式
     * @return 日期
     */
    public static Date StringToDate(String date, String parttern){
        Date myDate = null;
        if(date != null){
            try{
                myDate = getDateFormat(parttern).parse(date);
            }catch(Exception e){
            }
        }
        return myDate;
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     *
     * @param date
     *            日期字符串
     * @param pattern
     *            日期格式
     * @return 日期
     */
    public static Date stringToDate(String date, String pattern, String timeZone){
        Date myDate = null;
        if(date != null){
            try{
                SimpleDateFormat format = getDateFormat(pattern);
                format.setTimeZone(TimeZone.getTimeZone(timeZone));
                myDate = format.parse(date);
            }catch(Exception e){
                LOGGER.error("转换日期失败, date:{}",date, e);
                return null;
            }
        }
        return myDate;
    }

    public static String utcToGmt(String strDate){
        Date date = DateUtil.stringToDate(strDate, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue(),"UTC");
        return DateToString(date, DateStyle.YYYY_MM_DD_HH_MM_SS);
    }

    public static Date utcToGmtDate(String strDate){
        return DateUtil.stringToDate(strDate, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue(),"UTC");
    }

    public static int getTimeInt(Date date){
        return (int) (date.getTime()/1000);
    }

    /**
     * 将日期字符串转化为日期。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param dateStyle
     *            日期风格
     * @return 日期
     */
    public static Date StringToDate(String date, DateStyle dateStyle){
        Date myDate = null;
        if(dateStyle == null){
            List<Long> timestamps = new ArrayList<Long>();
            for(DateStyle style : DateStyle.values()){
                Date dateTmp = StringToDate(date, style.getValue());
                if(dateTmp != null){
                    timestamps.add(dateTmp.getTime());
                }
            }
            myDate = getAccurateDate(timestamps);
        }else{
            myDate = StringToDate(date, dateStyle.getValue());
        }
        return myDate;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     * 
     * @param date
     *            日期
     * @param parttern
     *            日期格式
     * @return 日期字符串
     */
    public static String DateToString(Date date, String parttern){
        String dateString = null;
        if(date != null){
            try{
                dateString = getDateFormat(parttern).format(date);
            }catch(Exception e){
            }
        }
        return dateString;
    }

    /**
     * 将日期转化为日期字符串。失败返回null。
     * 
     * @param date
     *            日期
     * @param dateStyle
     *            日期风格
     * @return 日期字符串
     */
    public static String DateToString(Date date, DateStyle dateStyle){
        String dateString = null;
        if(dateStyle != null){
            dateString = DateToString(date, dateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     * 
     * @param date
     *            旧日期字符串
     * @param parttern
     *            新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, String parttern){
        return StringToString(date, null, parttern);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     * 
     * @param date
     *            旧日期字符串
     * @param dateStyle
     *            新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, DateStyle dateStyle){
        return StringToString(date, null, dateStyle);
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     * 
     * @param date
     *            旧日期字符串
     * @param olddParttern
     *            旧日期格式
     * @param newParttern
     *            新日期格式
     * @return 新日期字符串
     */
    public static String StringToString(String date, String olddParttern, String newParttern){
        String dateString = null;
        if(olddParttern == null){
            DateStyle style = getDateStyle(date);
            if(style != null){
                Date myDate = StringToDate(date, style.getValue());
                dateString = DateToString(myDate, newParttern);
            }
        }else{
            Date myDate = StringToDate(date, olddParttern);
            dateString = DateToString(myDate, newParttern);
        }
        return dateString;
    }

    /**
     * 将日期字符串转化为另一日期字符串。失败返回null。
     * 
     * @param date
     *            旧日期字符串
     * @param olddDteStyle
     *            旧日期风格
     * @param newDateStyle
     *            新日期风格
     * @return 新日期字符串
     */
    public static String StringToString(String date, DateStyle olddDteStyle, DateStyle newDateStyle){
        String dateString = null;
        if(olddDteStyle == null){
            DateStyle style = getDateStyle(date);
            dateString = StringToString(date, style.getValue(), newDateStyle.getValue());
        }else{
            dateString = StringToString(date, olddDteStyle.getValue(), newDateStyle.getValue());
        }
        return dateString;
    }

    /**
     * 增加日期的年份。失败返回null。
     * 
     * @param date
     *            日期
     * @param yearAmount
     *            增加数量。可为负数
     * @return 增加年份后的日期字符串
     */
    public static String addYear(String date, int yearAmount){
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的年份。失败返回null。
     * 
     * @param date
     *            日期
     * @param yearAmount
     *            增加数量。可为负数
     * @return 增加年份后的日期
     */
    public static Date addYear(Date date, int yearAmount){
        return addInteger(date, Calendar.YEAR, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     * 
     * @param date
     *            日期
     * @param yearAmount
     *            增加数量。可为负数
     * @return 增加月份后的日期字符串
     */
    public static String addMonth(String date, int yearAmount){
        return addInteger(date, Calendar.MONTH, yearAmount);
    }

    /**
     * 增加日期的月份。失败返回null。
     * 
     * @param date
     *            日期
     * @param yearAmount
     *            增加数量。可为负数
     * @return 增加月份后的日期
     */
    public static Date addMonth(Date date, int yearAmount){
        return addInteger(date, Calendar.MONTH, yearAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param dayAmount
     *            增加数量。可为负数
     * @return 增加天数后的日期字符串
     */
    public static String addDay(String date, int dayAmount){
        return addInteger(date, Calendar.DATE, dayAmount);
    }
    
    /**
     * 增加日期的天数。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param dayAmount
     *            增加数量。可为负数
     * @param style
     * 			  日期样式
     * @return 增加天数后的日期字符串
     */
    public static String addDay(String date, int dayAmount, DateStyle style){
        return addInteger(date, style, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的天数。失败返回null。
     * 
     * @param date
     *            日期
     * @param dayAmount
     *            增加数量。可为负数
     * @return 增加天数后的日期
     */
    public static Date addDay(Date date, int dayAmount){
        return addInteger(date, Calendar.DATE, dayAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param hourAmount
     *            增加数量。可为负数
     * @return 增加小时后的日期字符串
     */
    public static String addHour(String date, int hourAmount){
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的小时。失败返回null。
     * 
     * @param date
     *            日期
     * @param hourAmount
     *            增加数量。可为负数
     * @return 增加小时后的日期
     */
    public static Date addHour(Date date, int hourAmount){
        return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param hourAmount
     *            增加数量。可为负数
     * @return 增加分钟后的日期字符串
     */
    public static String addMinute(String date, int hourAmount){
        return addInteger(date, Calendar.MINUTE, hourAmount);
    }

    /**
     * 增加日期的分钟。失败返回null。
     * 
     * @param date
     *            日期
     * @param minuteAmount
     *            增加数量。可为负数
     * @return 增加分钟后的日期
     */
    public static Date addMinute(Date date, int minuteAmount){
        return addInteger(date, Calendar.MINUTE, minuteAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @param hourAmount
     *            增加数量。可为负数
     * @return 增加秒钟后的日期字符串
     */
    public static String addSecond(String date, int hourAmount){
        return addInteger(date, Calendar.SECOND, hourAmount);
    }

    /**
     * 增加日期的秒钟。失败返回null。
     * 
     * @param date
     *            日期
     * @param hourAmount
     *            增加数量。可为负数
     * @return 增加秒钟后的日期
     */
    public static Date addSecond(Date date, int hourAmount){
        return addInteger(date, Calendar.SECOND, hourAmount);
    }

    public static Date addTime(Date date, String timeUnit, int amount){
        timeUnit = timeUnit.toLowerCase();
        switch (timeUnit){
            case TIME_UNIT_SECOND:
                return addInteger(date, Calendar.SECOND, amount);
            case TIME_UNIT_MINUTE:
                return addInteger(date, Calendar.MINUTE, amount);
            case TIME_UNIT_HOUR:
                return addInteger(date, Calendar.HOUR, amount);
            case TIME_UNIT_DAY:
                return addInteger(date, Calendar.DAY_OF_YEAR, amount);
            default:
                return date;
        }
    }

    /**
     * 获取日期的年份。失败返回0。
     * 
     * @param date
     *            日期字符串
     * @return 年份
     */
    public static int getYear(String date){
        return getYear(StringToDate(date));
    }

    /**
     * 获取日期的年份。失败返回0。
     * 
     * @param date
     *            日期
     * @return 年份
     */
    public static int getYear(Date date){
        return getInteger(date, Calendar.YEAR);
    }

    /**
     * 获取日期的月份。失败返回0。
     * 
     * @param date
     *            日期字符串
     * @return 月份
     */
    public static int getMonth(String date){
        return getMonth(StringToDate(date));
    }

    /**
     * 获取日期的月份。失败返回0。
     * 
     * @param date
     *            日期
     * @return 月份
     */
    public static int getMonth(Date date){
        return getInteger(date, Calendar.MONTH);
    }

    /**
     * 获取日期的天数。失败返回0。
     * 
     * @param date
     *            日期字符串
     * @return 天
     */
    public static int getDay(String date){
        return getDay(StringToDate(date));
    }

    /**
     * 获取日期的天数。失败返回0。
     * 
     * @param date
     *            日期
     * @return 天
     */
    public static int getDay(Date date){
        return getInteger(date, Calendar.DATE);
    }

    /**
     * 获取日期的小时。失败返回0。
     * 
     * @param date
     *            日期字符串
     * @return 小时
     */
    public static int getHour(String date){
        return getHour(StringToDate(date));
    }

    /**
     * 获取日期的小时。失败返回0。
     * 
     * @param date
     *            日期
     * @return 小时
     */
    public static int getHour(Date date){
        return getInteger(date, Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取日期的分钟。失败返回0。
     * 
     * @param date
     *            日期字符串
     * @return 分钟
     */
    public static int getMinute(String date){
        return getMinute(StringToDate(date));
    }

    /**
     * 获取日期的分钟。失败返回0。
     * 
     * @param date
     *            日期
     * @return 分钟
     */
    public static int getMinute(Date date){
        return getInteger(date, Calendar.MINUTE);
    }

    /**
     * 获取日期的秒钟。失败返回0。
     * 
     * @param date
     *            日期字符串
     * @return 秒钟
     */
    public static int getSecond(String date){
        return getSecond(StringToDate(date));
    }

    /**
     * 获取日期的秒钟。失败返回0。
     * 
     * @param date
     *            日期
     * @return 秒钟
     */
    public static int getSecond(Date date){
        return getInteger(date, Calendar.SECOND);
    }

    /**
     * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @return 日期
     */
    public static String getDate(String date){
        return StringToString(date, DateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期。默认yyyy-MM-dd格式。失败返回null。
     * 
     * @param date
     *            日期
     * @return 日期
     */
    public static String getDate(Date date){
        return DateToString(date, DateStyle.YYYY_MM_DD);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     * 
     * @param date
     *            日期字符串
     * @return 时间
     */
    public static String getTime(String date){
        return StringToString(date, DateStyle.HH_MM_SS);
    }

    /**
     * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
     * 
     * @param date
     *            日期
     * @return 时间
     */
    public static String getTime(Date date){
        return DateToString(date, DateStyle.HH_MM_SS);
    }


    /**
     * 获取两个日期相差的天数
     * 
     * @param date
     *            日期字符串
     * @param otherDate
     *            另一个日期字符串
     * @return 相差天数
     */
    public static int getIntervalDays(String date, String otherDate){
        return getIntervalDays(StringToDate(date), StringToDate(otherDate));
    }

    /**
     * @param date
     *            日期
     * @param otherDate
     *            另一个日期
     * @return 相差天数
     */
    public static int getIntervalDays(Date date, Date otherDate){
        date = DateUtil.StringToDate(DateUtil.getDate(date));
        long time = Math.abs(date.getTime() - otherDate.getTime());
        return (int) time / (24 * 60 * 60 * 1000);
    }

    public static String getWeekFromDate(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int week = c.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case 1:
                return "星期日";
            case 2:
                return "星期一";
            case 3:
                return "星期二";
            case 4:
                return "星期三";
            case 5:
                return "星期四";
            case 6:
                return "星期五";
            case 7:
                return "星期六";
            default:
                return "";
        }
    }

    /**
     * 获取指定天数之前的时间（相对于当前天数）
     * 
     * @Title: getLastDays
     * @param currentDate
     *            当前时间
     * @param dayNo
     *            当前时间的几天之前
     * @return
     * @date 2014-3-26 下午11:23:57
     * @author Lawrence
     */
    public static Date getLastTimeForDays(Date currentDate, Integer dayNo){
        if(null == currentDate || null == dayNo){
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, -dayNo);
        return c.getTime();
    }

    /**
     * 获取指定月数之前的时间（相对于当前天数）
     * 
     * @Title: getLastDays
     * @param currentDate
     *            当前时间
     * @param monthNo
     *            当前时间的几月之前
     * @return
     * @date 2014-3-26 下午11:23:57
     * @author Lawrence
     */
    public static Date getLastTimeForMonths(Date currentDate, Integer monthNo){
        if(null == currentDate || null == monthNo){
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.MONTH, -monthNo);
        return c.getTime();
    }

    /**
     * 获取指定月数之前的时间（相对于当前天数）
     * 
     * @Title: getLastDays
     * @param currentDate
     *            当前时间
     * @param yearNo
     *            当前时间的几年之前
     * @return
     * @date 2014-3-26 下午11:23:57
     * @author Lawrence
     */
    public static Date getLastTimeForYears(Date currentDate, Integer yearNo){
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.YEAR, -yearNo);
        return c.getTime();
    }


    /**
     * 获取当天00:00:00
     * @Title: getZeroPoint 
     * @Description: (这里用一句话描述这个方法的作用) 
     * @param currentDate
     * @return
     * @date 2014年10月10日 上午11:09:35  
     * @author Administrator
     */
    public static Date getZeroPoint(Date currentDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); // 把当前时间小时变成０
        cal.set(Calendar.MINUTE, 0); // 把当前时间分钟变成０
        cal.set(Calendar.SECOND, 0); // 把当前时间秒数变成０
        cal.set(Calendar.MILLISECOND, 0); // 把当前时间毫秒变成０
        return cal.getTime();
    }
    
	 /**
     * 把毫秒转化成日期
     * @param millSec(毫秒数)
     * @return
     */
    public static Date LongToDate(Long millSec){
    	return LongToDate(millSec,"yyyy-MM-dd HH:mm:ss");
    }
    
	 /**
     * 把毫秒转化成日期
     * @param parttern(日期格式，例如：MM/ dd/yyyy HH:mm:ss)
     * @param millSec(毫秒数)
     * @return
     */
    public static Date LongToDate(Long millSec,String parttern){
     return new Date(millSec);
    }

    public static String getDuration(Long millSec){
        long days = millSec / (1000 * 60 * 60 * 24);
        long hours = (millSec % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (millSec % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millSec % (1000 * 60)) / 1000;
        return (days>0 ? days + "d":"") + (hours>0?hours + "h":"") + (minutes>0? minutes + "m":"")
                + seconds + "s";
    }

    public static String getDuration(String millSecStr){
        return getDuration(Long.valueOf(millSecStr));
    }

    public static Integer getSinceSeconds(Integer num, String timeUnit){
        if(num == null || StringUtils.isBlank(timeUnit)){
            return null;
        }
        Integer sinceSeconds = 0;
        String unit = timeUnit.toLowerCase();
        switch (unit){
            case "m":
                sinceSeconds = 60 * num;
                break;
            case "h":
                sinceSeconds = 60 * 60 * num;
                break;
            case "d":
                sinceSeconds = 60 * 60 * 24 * num;
                break;
            default:
                sinceSeconds = 0;
        }
        return sinceSeconds;
    }

    /**
     * 根据时区获取带时区的日期格式
     * @param timeZone
     * @return
     */
    public static String getTimezoneFormatStyle(TimeZone timeZone){
        int hour = timeZone.getRawOffset()/ONE_HOUR_MILLISECONDS;
        int minute = Math.abs((timeZone.getRawOffset() - hour * ONE_HOUR_MILLISECONDS)/ONE_MINUTE_MILLISECONDS);
        String hourMinute = hour + "";
        if(hour> -10 && hour < 0){
            hourMinute = "-0" + Math.abs(hour);
        }else if(hour >= 0 && hour<10){
            hourMinute = "+0" + hourMinute;
        }else if(hour>9){
            hourMinute =  "+" + hourMinute;
        }
        if(minute == 0) {
            hourMinute += ":00";
        }else{
            hourMinute += ":" + minute;
        }
        String style = "yyyy-MM-dd'T'HH:mm:ss'"+hourMinute+"'";
        return  style;
    }

}
