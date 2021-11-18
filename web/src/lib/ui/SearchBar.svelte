<script>
    import {createEventDispatcher, onMount} from "svelte";
    import {variables} from "$lib/variables";
    import FilterBox from "$lib/ui/FilterBox.svelte";

    export let storeFilter = "all";
    export let distanceFilter = -1;
    export let sortStrategy = "alphabetical";

    let dispatch = createEventDispatcher();
    let searchBox, filterContainer, sortSection;
    let displayFilters = false;

    let allStores;
    let filterStores = [];

    onMount(() => {
        variables.validStores.forEach(str => filterStores.push({name: str, checked: false}));
        filterStores = [...filterStores];
    });

    const search = (e) => {
        sortStrategy = sortSection.querySelector("input[type='radio']:checked").value;
        if (!e || e.keyCode == 13) {
            dispatch("search", {
                search: searchBox.value,
                filters: {
                    stores: storeFilter,
                    distance: distanceFilter,
                    sortStrategy
                }
            });
        }
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

    const selectedSort = e => {
        if (e.target.checked)
            sortStrategy = e.target.value;
        console.log(sortStrategy);
    }

</script>

<FilterBox on:apply={() => search()}>
    <!--  TODO Get sorting working  -->
    <div bind:this={sortSection} class="filter-section">
        <h4>Sort</h4>
        <label>
            <input checked name="sorting"
                   on:click={selectedSort}
                   type=radio
                   value="alphabetical">A-Z
        </label>
        <label>
            <input name="sorting"
                   on:click={selectedSort}
                   type=radio
                   value="lowest_first">Price (Lowest First)
        </label>
        <label>
            <input name="sorting"
                   on:click={selectedSort}
                   type=radio
                   value="highest_first">Price (Highest First)
        </label>
    </div>
    <div class="filter-section">
        <h4>Stores</h4>
        <label>
            <input bind:this={allStores} checked on:click={clickAll} type="checkbox" value="all"/>
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
    </div>
    <div class="filter-section">
        <h4>Distance</h4>
        <label for="dist">Distance (mi):
            <input bind:value={distanceFilter} id="dist" max="2500" min="-1" name="dist" placeholder="10"
                   type="number">
        </label>
    </div>
</FilterBox>
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
</style>