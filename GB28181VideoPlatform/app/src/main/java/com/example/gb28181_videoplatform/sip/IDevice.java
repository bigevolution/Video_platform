/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package com.example.gb28181_videoplatform.sip;


public interface IDevice {

    void Register(int state);

    void Heart();

    void Call(String to);

    void Accept();

    void Reject();

    void Cancel();

    void Hangup();

    void invite(String to, int port, String nvrSipId, String ipcSipId, boolean isNvr);

    void Bye();

    void SendMessage(String targetName, String deviceId, String to, String message);

    void SendDTMF(String digit);

    void Mute(boolean muted);

}
