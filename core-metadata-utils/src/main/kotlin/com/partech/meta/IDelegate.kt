/*
Copyright (C) 2021 PAR Government

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information, please email nathan_bedell@partech.com
*/

package com.partech.meta

import kotlin.reflect.KProperty

/** Helper interface for defining custom delegates. */
interface IDelegate<X, A> {
    operator fun getValue(value: X, property: KProperty<*>): A
    operator fun setValue(ctx: X, property: KProperty<*>, value: A)
}