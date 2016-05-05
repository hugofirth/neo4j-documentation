/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.raft.log.physical;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.neo4j.coreedge.raft.log.DummyRaftableContentSerializer;
import org.neo4j.coreedge.raft.log.RaftLog;
import org.neo4j.coreedge.raft.log.RaftLogContractTest;
import org.neo4j.graphdb.mockfs.EphemeralFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.logging.NullLogProvider;

// TODO: Separate into small-cache and no-cache tests. Perhaps parameterize this one (0, 5, 1024) cache sizes.
public class NewPhysicalRaftLogContractTest extends RaftLogContractTest
{
    private NewPhysicalRaftLog raftLog;
    private LifeSupport life = new LifeSupport();
    private FileSystemAbstraction fileSystem;

    @Override
    public RaftLog createRaftLog() throws IOException
    {
        this.raftLog = createRaftLog( 0 );
        return raftLog;
    }

    @After
    public void tearDown() throws Throwable
    {
        life.stop();
        life.shutdown();
    }

    private NewPhysicalRaftLog createRaftLog( int cacheSize )
    {
        if ( fileSystem == null )
        {
            fileSystem = new EphemeralFileSystemAbstraction();
        }
        File directory = new File( "raft-log" );
        fileSystem.mkdir( directory );

        NewPhysicalRaftLog newRaftLog = new NewPhysicalRaftLog( fileSystem, directory, 10 * 1024,
                new DummyRaftableContentSerializer(),
                NullLogProvider.getInstance(), cacheSize );
        life.add( newRaftLog );
        life.init();
        life.start();
        return newRaftLog;
    }

//    @Test
//    public void shouldReadBackInCachedEntry() throws Throwable
//    {
//        // Given
//        NewPhysicalRaftLog raftLog = (NewPhysicalRaftLog) createRaftLog();
//        int term = 0;
//        ReplicatedInteger content = ReplicatedInteger.valueOf( 4 );
//
//        // When
//        long entryIndex = raftLog.append( new RaftLogEntry( term, content ) );
//
//        // Then
//        assertEquals( entryIndex, raftLog.appendIndex() );
//        assertEquals( content, readLogEntry( raftLog, entryIndex ).content() );
//        assertEquals( term, raftLog.readEntryTerm( entryIndex ) );
//    }
//
//    @Test
//    public void shouldReadBackNonCachedEntry() throws Exception
//    {
//        // Given
//        int cacheSize = 1;
//        NewPhysicalRaftLog raftLog = createRaftLog( cacheSize );
//        int term = 0;
//        ReplicatedInteger content1 = ReplicatedInteger.valueOf( 4 );
//        ReplicatedInteger content2 = ReplicatedInteger.valueOf( 5 );
//
//        // When
//        long entryIndex1 = raftLog.append( new RaftLogEntry( term, content1 ) );
//        long entryIndex2 = raftLog.append( new RaftLogEntry( term, content2 ) ); // this will push the first entry out of cache
//
//        // Then
//        // entry 1 should be there
//        assertEquals( content1, readLogEntry( raftLog, entryIndex1 ).content() );
//        assertEquals( term, raftLog.readEntryTerm( entryIndex1 ) );
//
//        // entry 2 should be there also
//        assertEquals( content2, readLogEntry( raftLog, entryIndex2 ).content() );
//        assertEquals( term, raftLog.readEntryTerm( entryIndex2 ) );
//    }
//
//    @Test
//    public void shouldRestoreCommitIndexOnStartup() throws Throwable
//    {
//        // Given
//        NewPhysicalRaftLog raftLog = createRaftLog( 100 /* cache size */  );
//        int term = 0;
//        ReplicatedInteger content1 = ReplicatedInteger.valueOf( 4 );
//        ReplicatedInteger content2 = ReplicatedInteger.valueOf( 5 );
//        raftLog.append( new RaftLogEntry( term, content1 ) );
//        long entryIndex2 = raftLog.append( new RaftLogEntry( term, content2 ) );
//
//        // When
//        // we restart the raft log
//        life.remove( raftLog ); // stops the removed instance
//        raftLog = createRaftLog( 100 );
//
//        // Then
//        assertEquals( entryIndex2, raftLog.appendIndex() );
//    }
//
//    @Test
//    public void shouldRestoreCorrectCommitAndAppendIndexOnStartupAfterTruncation() throws Exception
//    {
//        // Given
//        NewPhysicalRaftLog raftLog = createRaftLog( 100 /* cache size */  );
//        int term = 0;
//        ReplicatedInteger content = ReplicatedInteger.valueOf( 4 );
//        raftLog.append( new RaftLogEntry( term, content ) );
//        raftLog.append( new RaftLogEntry( term, content ) );
//        long entryIndex3 = raftLog.append( new RaftLogEntry( term, content ) );
//        long entryIndex4 = raftLog.append( new RaftLogEntry( term, content ) );
//
//        raftLog.truncate( entryIndex4 );
//
//        // When
//        // we restart the raft log
//        life.remove( raftLog ); // stops the removed instance
//        raftLog = createRaftLog( 100 );
//
//        // Then
//        assertEquals( entryIndex3, raftLog.appendIndex() );
//    }
//
//    @Test
//    public void shouldRestoreCorrectCommitAndAppendIndexWithTruncationRecordsAndAppendedRecordsAfterThat() throws Exception
//    {
//        // Given
//        NewPhysicalRaftLog raftLog = createRaftLog( 100 /* cache size */  );
//        int term = 0;
//        ReplicatedInteger content = ReplicatedInteger.valueOf( 4 );
//        raftLog.append( new RaftLogEntry( term, content ) );
//        raftLog.append( new RaftLogEntry( term, content ) );
//        raftLog.append( new RaftLogEntry( term, content ) );
//        long entryIndex4 = raftLog.append( new RaftLogEntry( term, content ) );
//
//        raftLog.truncate( entryIndex4 );
//
//        long entryIndex5 = raftLog.append( new RaftLogEntry( term, content ) );
//
//        // When
//        // we restart the raft log
//        life.remove( raftLog ); // stops the removed instance
//        raftLog = createRaftLog( 100 );
//
//        // Then
//        assertEquals( entryIndex5, raftLog.appendIndex() );
//    }
}
