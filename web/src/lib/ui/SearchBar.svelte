<script>
    import {createEventDispatcher, onMount} from "svelte";
    import {variables} from "$lib/variables";

    export let storeFilter = "all";
    export let distanceFilter = -1;

    let dispatch = createEventDispatcher();
    let searchBox, filterContainer;
    let displayFilters = false;

    let allStores;
    let filterStores = [];

    onMount(() => {
        variables.validStores.forEach(str => filterStores.push({name: str, checked: false}));
        filterStores = [...filterStores];
    });

    const search = (e) => {
        if (!e || e.keyCode == 13) {
            dispatch("search", {search: searchBox.value, filters: {stores: storeFilter, distance: distanceFilter}});
        }
    };

    const activateFilters = () => {
        displayFilters = !displayFilters;
        if (displayFilters)
            filterContainer.show();
        else
            filterContainer.close();
    };

    $: if (filterContainer && displayFilters) {
        setTimeout(() => {
            window.onclick = function (event) {
                if (event.target != filterContainer && !filterContainer.contains(event.target)) {
                    if (filterContainer.hasAttribute("open")) {
                        filterContainer.close();
                        displayFilters = false;
                    }
                }
            }
        }, 100);
    } else if (filterContainer) {
        window.onclick = undefined;
    }

    const handleFilterClick = store => {
        store.checked = !store.checked;
        if (store.checked) {
            if (allStores)
                allStores.checked = false;
        }

        storeFilter = "";
        filterStores.forEach(str => {
            if (str.checked)
                storeFilter += str.name + ",";
        });
        storeFilter = storeFilter.substring(0, storeFilter.length - 1);
    };

    const clickAll = e => {
        if (e.target.checked) {
            filterStores.forEach(str => {
                str.checked = false;
            });

            filterStores = [...filterStores];
            storeFilter = "all";
        }
    };

</script>

<div class="filter-box">
    <span class="material-icons-round filter-btn" on:click={activateFilters}>filter_alt</span>
    <dialog bind:this={filterContainer} class="filter-container">
        <h4>Stores</h4>
        <hr/>
        <label>
            <input bind:this={allStores} on:click={clickAll} type="checkbox" value="all" checked/>
            All
        </label>
        {#each filterStores as validStore (validStore.name)}
            <label>
                <input name="store" type="checkbox" bind:checked={validStore.checked} on:click={() =>
                handleFilterClick(validStore)}
                       value="{validStore.name}"/>
                {validStore.name}
            </label>
        {/each}
        <h4>Distance</h4>
        <hr/>
        <label for="dist">Distance (mi):
            <input bind:value={distanceFilter} id="dist" max="2500" min="-1" name="dist" placeholder="10"
                   type="number">
        </label>
        <div class="button" on:click={() => search()}>Apply Filters</div>
    </dialog>
</div>
<div class="search">
    <span class="material-icons-round">search</span>
    <input bind:this={searchBox} on:keyup={search} placeholder="Search..."/>
</div>


<style>
    .search {
        display: flex;
        align-items: stretch;
        border: 2px solid #ccc;
        border-radius: 0.2em;
        margin-left: 0.4em;
    }

    .search span {
        color: rgba(0, 0, 0, 0.54);
    }

    .search input {
        border: none;
    }

    .search input:focus {
        outline: none;
    }

    .filter-btn {
        cursor: pointer;
        user-select: none;
    }

    .filter-box {
        position: relative;
    }

    .filter-container {
        position: absolute;
        max-width: 300px;
        width: 20em;
        left: 100%;
        transform: translateX(-50%);
        background: #ddd;
        z-index: 10;
        padding: 1em;
        box-shadow: 5px 5px 5px #666;
        tab-index: 0;
    }

    h4 {
        margin: 0;
    }

    label {
        display: flex;
        align-items: center;
    }

    input[type="number"] {
        margin-left: 0.5em;
    }

    input[type="checkbox"] {
        margin-right: 0.5em;
    }

    .button {
        margin: 0.5em 0 0 0;
    }
</style>