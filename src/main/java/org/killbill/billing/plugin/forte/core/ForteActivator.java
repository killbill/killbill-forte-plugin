/*
 * Copyright 2014 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.forte.core;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.plugin.forte.api.FortePaymentPluginApi;
import org.killbill.billing.plugin.forte.dao.ForteDao;
import org.killbill.clock.Clock;
import org.killbill.clock.DefaultClock;
import org.killbill.killbill.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillEventDispatcher.OSGIKillbillEventHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class ForteActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "killbill-forte";

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        // For safety, we want to enter with context class loader set to the Felix bundle and not the one from the web app.
        // This is not strictly required in that initialization but if we were to do fancy things (like initializing apache cxf),
        // that would break without it.
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        // Register the servlet
        final ForteServlet forteServlet = new ForteServlet();
        registerServlet(context, forteServlet);

        // Register the payment plugin
        final Clock clock = new DefaultClock();
        final ForteDao dao = new ForteDao(dataSource.getDataSource());
        final FortePaymentPluginApi pluginApi = new FortePaymentPluginApi(killbillAPI, configProperties, logService, clock, dao);
        registerPaymentPluginApi(context, pluginApi);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        logService.log(LogService.LOG_INFO, "ForteActivator stopping");
        super.stop(context);
    }

    @Override
    public OSGIKillbillEventHandler getOSGIKillbillEventHandler() {
        return null;
    }

    private void registerServlet(final BundleContext context, final HttpServlet servlet) {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Servlet.class, servlet, props);
    }

    private void registerPaymentPluginApi(final BundleContext context, final PaymentPluginApi api) {
        final Dictionary props = new Hashtable();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, PaymentPluginApi.class, api, props);
    }
}
