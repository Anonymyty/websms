/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of WebSMS.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.websms.connector.common;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * {@link AsyncTask} run by the Connector's {@link ConnectorService}.
 * 
 * @author flx
 */
final class ConnectorTask extends AsyncTask<Void, Void, Void> {

	/** Intent comming from outside. */
	private final Intent intent;
	/** Connector class which will do the actual IO. */
	private final Connector receiver;
	/** Used connector. */
	private final ConnectorSpec connector;
	/** Command running. */
	private final ConnectorCommand command;
	/** {@link ConnectorService}. */
	private final ConnectorService service;

	/**
	 * Create a connector task.
	 * 
	 * @param i
	 *            intent holding {@link ConnectorSpec} and
	 *            {@link ConnectorCommand}
	 * @param r
	 *            {@link Connector}
	 * @param s
	 *            {@link ConnectorService}
	 */
	public ConnectorTask(final Intent i, final Connector r,
			final ConnectorService s) {
		this.intent = i;
		this.connector = new ConnectorSpec(i);
		this.command = new ConnectorCommand(i);
		this.receiver = r;
		this.service = s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(final Void... arg0) {
		try {
			switch (this.command.getType()) {
			case ConnectorCommand.TYPE_BOOTSTRAP:
				this.receiver.doBootstrap(this.service, this.intent);
				break;
			case ConnectorCommand.TYPE_UPDATE:
				this.receiver.doUpdate(this.service, this.intent);
				break;
			case ConnectorCommand.TYPE_SEND:
				this.receiver.doSend(this.service, this.intent);
				break;
			default:
				break;
			}
		} catch (WebSMSException e) {
			Log.d(this.connector.getID(), "error in AsyncTask", e);
			// put error message to ConnectorSpec
			this.connector.setErrorMessage(e);
		} catch (Exception e) {
			Log.e(this.connector.getID(), "error in AsyncTask", e);
			// put error message to ConnectorSpec
			this.connector.setErrorMessage(e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final Void result) {
		final String e = this.connector.getErrorMessage();
		if (e != null) {
			Toast.makeText(this.service, e, Toast.LENGTH_LONG).show();
		}
		final Intent i = this.connector.setToIntent(null);
		this.command.setToIntent(i);
		this.service.sendBroadcast(i);
		this.service.unregister(i);
	}
}
