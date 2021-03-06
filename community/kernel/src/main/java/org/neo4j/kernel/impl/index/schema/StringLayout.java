/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.index.schema;

import org.neo4j.index.internal.gbptree.Layout;
import org.neo4j.io.pagecache.PageCursor;

import static java.lang.String.format;
import static org.neo4j.kernel.impl.index.schema.StringIndexKey.ENTITY_ID_SIZE;

/**
 * {@link Layout} for strings.
 */
class StringLayout extends IndexLayout<StringIndexKey>
{
    StringLayout()
    {
        super( "USI", 0, 1 );
    }

    @Override
    public StringIndexKey newKey()
    {
        return new StringIndexKey();
    }

    @Override
    public StringIndexKey copyKey( StringIndexKey key, StringIndexKey into )
    {
        into.copyFrom( key );
        return into;
    }

    @Override
    public int keySize( StringIndexKey key )
    {
        return key.size();
    }

    @Override
    public void writeKey( PageCursor cursor, StringIndexKey key )
    {
        cursor.putLong( key.getEntityId() );
        cursor.putBytes( key.bytes, 0, key.bytesLength );
    }

    @Override
    public void readKey( PageCursor cursor, StringIndexKey into, int keySize )
    {
        if ( keySize < ENTITY_ID_SIZE )
        {
            into.setEntityId( Long.MIN_VALUE );
            into.setBytesLength( 0 );
            cursor.setCursorException( format( "Reading string index key with an unexpected keySize:%d", keySize ) );
            return;
        }
        into.setEntityId( cursor.getLong() );
        int bytesLength = keySize - ENTITY_ID_SIZE;
        into.setBytesLength( bytesLength );
        cursor.getBytes( into.bytes, 0, bytesLength );
    }

    @Override
    public boolean fixedSize()
    {
        return false;
    }

    @Override
    public void minimalSplitter( StringIndexKey left, StringIndexKey right, StringIndexKey into )
    {
        int maxLength = Math.min( left.bytesLength, right.bytesLength );
        int targetLength = 0;
        for ( ; targetLength < maxLength; targetLength++ )
        {
            if ( left.bytes[targetLength] != right.bytes[targetLength] )
            {
                // Convert to length from array index
                targetLength++;
                break;
            }
        }
        into.copyFrom( right, targetLength );
    }

    @Override
    public String toString()
    {
        return format( "%s[version:%d.%d, identifier:%d]", getClass().getSimpleName(), majorVersion(), minorVersion(), identifier() );
    }

    @Override
    int compareValue( StringIndexKey o1, StringIndexKey o2 )
    {
        return o1.compareValueTo( o2 );
    }
}
