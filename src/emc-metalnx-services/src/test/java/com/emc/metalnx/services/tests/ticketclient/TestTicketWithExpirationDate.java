/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.tests.ticketclient;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketClientService;
import com.emc.metalnx.services.tests.tickets.TestTicketUtils;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestTicketWithExpirationDate {
    private static final Date EXPIRATION_DATE = new Date();

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private TicketClientService ticketClientService;

    @Autowired
    private IRODSServices irodsServices;

    private String targetPath, ticketString;
    private TestTicketUtils ticketUtils;
    private File localFile;

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
        ticketString = ticketUtils.createTicket(parentPath, username);
        ticketUtils.setExpirationDate(ticketString, yesterday());
    }

    @After
    public void tearDown() throws JargonException {
        FileUtils.deleteQuietly(localFile);
        ticketUtils.deleteAllTickets();
    }

    @Test(expected = DataGridTicketUploadException.class)
    public void testTicketWithExpirationDate() throws IOException, DataGridTicketUploadException {
        localFile = ticketUtils.createLocalFile("test-ticket-" + System.currentTimeMillis());
        ticketClientService.transferFileToIRODSUsingTicket(ticketString, localFile, targetPath);
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
}
