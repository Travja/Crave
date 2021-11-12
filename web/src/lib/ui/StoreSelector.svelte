<script>
    import {variables} from "$lib/variables";
    import {slide} from "svelte/transition";
    import {onMount} from "svelte";

    export let store;
    let position;
    let storeSelect,
        addressInput,
        cityInput,
        stateInput;
    const getLocation = (callback) => {
        if (navigator && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(pos => {
                position = pos;
                callback();
            });
        } else {
            alert("Geolocation is not supported by this browser.");
        }
    };

    const toRads = (degrees) => {
        let pi = Math.PI;
        return degrees * (pi / 180);
    };

    let url = "https://atlas.microsoft.com/search/fuzzy/json" +
        "?api-version=1.0" +
        "&query={query}" +
        "&subscription-key=" + variables.subKey +
        "&lat={lat}" +
        "&lon={lon}";
    let strs = [];
    export let selected;
    const updateStore = str => {
        if (selected == str || !str) return;

        selected = str;

        getLocation((store) => {
            if (!position) return;
            console.log(position.coords);

            let newUrl = url.replace("{query}", str)
                .replace("{lat}", position.coords.latitude.toString())
                .replace("{lon}", position.coords.longitude.toString());

            fetch(newUrl)
                .then(res => res.json())
                .then(data => {
                    strs = [];
                    let results = data.results;

                    for (let i = 0; i < 3; i++) {
                        let store = results[i];
                        let name = store.poi.name;
                        let address = store.address.freeformAddress;
                        let t1 = store.position.lat;
                        let n1 = store.position.lon;

                        let distance = haversineDistance({...store.position}, {
                            lat: position.coords.latitude,
                            lon: position.coords.longitude
                        });

                        strs.push({
                            name,
                            address,
                            lat: t1,
                            lon: n1,
                            distance: distance.toFixed(1),
                            selected: false
                        });
                    }
                })
                .catch(e => console.error(e));

        });
    };

    const haversineDistance = (c1 = {lat: 0, lon: 0}, c2 = {lat: 0, lon: 0}) => {
        let earthRadius = 3958.75;

        let t1 = c1.lat;
        let t2 = c2.lat;
        let n1 = c1.lon;
        let n2 = c2.lon;

        let hav = Math.pow(Math.sin(toRads((t2 - t1) / 2)), 2)    // sin^2((t2-t1)/2)
            + (Math.pow(Math.sin(toRads((n2 - n1) / 2)), 2)       // sin^2((n2-n1)/2)
                * Math.cos(toRads(t2)) * Math.cos(toRads(t1)));     // cos(t2)*cos(t1)

        // distance = 2r*arcsin(√(sin^2((t2-t1)/2) + cos(t2)cos(t1)sin^2((n2-n1)/2)))
        let distance = 2 * earthRadius * Math.asin(Math.sqrt(hav));
        return distance;
    };

    const selectStore = (e, i) => {
        for (let j = 0; j < strs.length; j++) {
            if (j == i) continue;
            strs[j].selected = false;
        }
        strs[i].selected = !strs[i].selected;
        storeSelect.value = strs[i].name;

        let address = strs[i].address;
        let split = address.split(", ");

        addressInput.value = split[0];
        stateInput.value = split[2].split(" ")[0];
        cityInput.value = split[1];
    };

    export const getSelectedStore = () => {
        for (const stor of strs) {
            if (stor.selected)
                return stor;
        }

        return null;
    };

    onMount(() => {
        if (store) {
            updateStore(store);
        }
    });
</script>

<div>
    <div class="hidden">
        <input bind:this={storeSelect} id="store-select" name="store-select" type="text"/>
        <input bind:this={addressInput} id="address" name="address" type="text"/>
        <input bind:this={stateInput} id="state" name="state" type="text"/>
        <input bind:this={cityInput} id="city" name="city" type="text"/>
    </div>
    <h4>Pick Your Store</h4>
    {#each variables.validStores as validStore}
        <label>
            <!-- disabled="{!!store}" -->
            <input name="store" on:click={() => updateStore(validStore)} type="radio"
                   checked="{validStore.toLowerCase() == store?.toLowerCase()}"/>
            {validStore}
        </label>
    {/each}
    <div class="stores">
        {#each strs as store, i}
            <div class="store"
                 class:selected={store.selected}
                 in:slide
                 on:click={e => selectStore(e, i)}>{i + 1}) {store.name} - {store.address}
                (~{store.distance}mi)
            </div>
        {/each}
    </div>

    <p>Store not supported? Contact us and make a suggestion!</p>
</div>

<style>
    .hidden {
        display: none;
    }
    
    label {
        user-select: none;
    }

    .stores {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        justify-content: center;
        margin: 0.5em;
    }

    .store {
        text-align: left;
        width: 100%;
        background: #ccc;
        padding: 0.8em;
        box-sizing: border-box;
        border-bottom: 1px solid black;
    }

    .store.selected {
        border: 2px solid blue;
    }

    .store:hover {
        background: #aaa;
        cursor: pointer;
    }
</style>