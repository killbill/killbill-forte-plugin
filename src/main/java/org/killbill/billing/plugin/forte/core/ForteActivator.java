/*
 * Copyright 2014-2015 The Billing Project, LLC
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

import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.payment.plugin.api.PaymentPluginApi;
import org.killbill.billing.plugin.api.notification.PluginConfigurationEventHandler;
import org.killbill.billing.plugin.forte.api.FortePaymentPluginApi;
import org.killbill.billing.plugin.forte.client.ForteAGIClient;
import org.killbill.billing.plugin.forte.client.ForteWSClient;
import org.killbill.billing.plugin.forte.dao.ForteDao;
import org.killbill.clock.Clock;
import org.killbill.clock.DefaultClock;
import org.killbill.killbill.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillEventDispatcher.OSGIKillbillEventHandler;
import org.osgi.framework.BundleContext;

public class ForteActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "killbill-forte";

    private ForteAGIConfigurationHandler forteAGIConfigurationHandler;
    private ForteWSConfigurationHandler forteWSConfigurationHandler;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        final Clock clock = new DefaultClock();
        final ForteDao dao = new ForteDao(dataSource.getDataSource());

        // Register the servlet
        final ForteServlet forteServlet = new ForteServlet();
        registerServlet(context, forteServlet);

        final ForteAGIClient globalForteAGIClient = forteAGIConfigurationHandler.createConfigurable(configProperties.getProperties());
        forteAGIConfigurationHandler.setDefaultConfigurable(globalForteAGIClient);

        final ForteWSClient globalForteWSClient = forteWSConfigurationHandler.createConfigurable(configProperties.getProperties());
        forteWSConfigurationHandler.setDefaultConfigurable(globalForteWSClient);

        // Register the payment plugin
        final FortePaymentPluginApi pluginApi = new FortePaymentPluginApi(forteAGIConfigurationHandler, forteWSConfigurationHandler, killbillAPI, configProperties, logService, clock, dao);
        registerPaymentPluginApi(context, pluginApi);
    }

    @Override
    public OSGIKillbillEventHandler getOSGIKillbillEventHandler() {
        forteAGIConfigurationHandler = new ForteAGIConfigurationHandler(PLUGIN_NAME, killbillAPI, logService);
        forteWSConfigurationHandler = new ForteWSConfigurationHandler(PLUGIN_NAME, killbillAPI, logService);
        return new PluginConfigurationEventHandler(forteAGIConfigurationHandler, forteWSConfigurationHandler);
    }

    private void registerServlet(final BundleContext context, final HttpServlet servlet) {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Servlet.class, servlet, props);
    }

    private void registerPaymentPluginApi(final BundleContext context, final PaymentPluginApi api) {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, PaymentPluginApi.class, api, props);
    }
}
