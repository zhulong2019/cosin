/*
 * Copyright (C) 2017 优客服-多渠道客服系统
 * Modifications copyright (C) 2018 Chatopera Inc, <https://www.chatopera.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chatopera.cc.concurrent.dsdata;

import com.lmax.disruptor.RingBuffer;

public class DSDataEventProducer {
    private final RingBuffer<DSDataEvent> ringBuffer;

    public DSDataEventProducer(RingBuffer<DSDataEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(DSData dsData) {
        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            DSDataEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            event.setDSData(dsData);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}