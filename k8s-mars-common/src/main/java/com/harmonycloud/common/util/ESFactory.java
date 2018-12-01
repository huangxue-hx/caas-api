package com.harmonycloud.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Component;

/**
 * Created by czm on 2017/3/28.
 */
@Component
public class ESFactory {

	public static final ExecutorService executor = Executors.newFixedThreadPool(20);
	




}