/*
 *   Copyright (c) 2017.  Jefferson Lab (JLab). All rights reserved. Permission
 *   to use, copy, modify, and distribute  this software and its documentation for
 *   governmental use, educational, research, and not-for-profit purposes, without
 *   fee and without a signed licensing agreement.
 *
 *   IN NO EVENT SHALL JLAB BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL
 *   INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
 *   OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF JLAB HAS
 *   BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   JLAB SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE. THE CLARA SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY,
 *   PROVIDED HEREUNDER IS PROVIDED "AS IS". JLAB HAS NO OBLIGATION TO PROVIDE
 *   MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *   This software was developed under the United States Government license.
 *   For more information contact author at gurjyan@jlab.org
 *   Department of Experimental Nuclear Physics, Jefferson Lab.
 */

package org.jlab.coda.afecs.system.util;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 *   My own version of the LinkedConcurrentHashMap
 *
 *
 * @author gurjyan
 *         Date: 12/4/14 Time: 1:57 PM
 * @version 4.x
 */
public class LinkedConcurrentHashMap<K, V> {

    private LinkedHashMap<K, V> linkedHashMap = null;
    private ReadWriteLock readWriteLock = null;

    public LinkedConcurrentHashMap() {
        this.linkedHashMap  = new LinkedHashMap<>();
        readWriteLock=new ReentrantReadWriteLock();
    }

    public void put(K key, V value) throws SQLException {
        Lock writeLock=readWriteLock.writeLock();
        try{
            writeLock.lock();
            linkedHashMap.put(key, value);
        }finally{
            writeLock.unlock();
        }
    }

    public V get(K key){
        Lock readLock=readWriteLock.readLock();
        try{
            readLock.lock();
            return linkedHashMap.get(key);
        }finally{
            readLock.unlock();
        }
    }

    public boolean containsKey(K key){
        Lock readLock=readWriteLock.readLock();
        try{
            readLock.lock();
            return linkedHashMap.containsKey(key);
        }finally{
            readLock.unlock();
        }
    }

    public V remove(K key){
        Lock writeLock=readWriteLock.writeLock();
        try{
            writeLock.lock();
            return linkedHashMap.remove(key);
        }finally{
            writeLock.unlock();
        }
    }

    public void clear(){
        linkedHashMap.clear();
    }

    public ReadWriteLock getLock(){
        return readWriteLock;
    }

    public Set<Map.Entry<K, V>> entrySet(){
        return linkedHashMap.entrySet();
    }

    public Collection<V> values(){
        return linkedHashMap.values();
    }
}
