/*
* Copyright 2021 Spotify AB
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.spotify.ruler.plugin.ownership

import com.spotify.ruler.models.ComponentType

/**
 * Encapsulates ownership information of different components.
 *
 * @param entries List of ownership entries parsed from the ownership file.
 * @param defaultOwner Owner which should be used if no explicit owner is defined.
 */
class OwnershipInfo(entries: List<OwnershipEntry>, private val defaultOwner: String) {
    private val explicitOwnershipEntries = mutableMapOf<String, String>()
    private val wildcardOwnershipEntries = mutableMapOf<String, String>()

    // Differentiate between explicit (full match) entries and wildcard entries
    init {
        entries.forEach { (identifier, owner) ->
            if (identifier.endsWith('*')) {
                wildcardOwnershipEntries[identifier.substringBeforeLast('*')] = owner
            } else {
                explicitOwnershipEntries[identifier] = owner
            }
        }
    }

    /**
     * Returns the owner of a given [component]. If the owner has no explicit owner, the [defaultOwner] will be returned
     * instead.
     */
    fun getOwner(component: String, componentType: ComponentType): String {
        val owner = when (componentType) {
            ComponentType.INTERNAL -> explicitOwnershipEntries[component]
            ComponentType.EXTERNAL -> explicitOwnershipEntries[component.substringBeforeLast(':')]
        }
        return owner ?: getWildcardOwner(component) ?: defaultOwner
    }

    /** Tries to find the owner for a component with the given [component] identifier based on all wildcard entries. */
    private fun getWildcardOwner(component: String): String? {
        val identifier = wildcardOwnershipEntries.keys
            .filter(component::startsWith) // Find all identifiers that match the wildcard
            .maxByOrNull(String::length) // Take the longest one because that one is the most specific

        return wildcardOwnershipEntries[identifier]
    }
}
