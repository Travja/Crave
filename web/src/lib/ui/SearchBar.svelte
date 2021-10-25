<script>
    import {createEventDispatcher} from "svelte";

    let dispatch = createEventDispatcher();
    let searchBox;

    const search = (e) => {
        if (e.keyCode == 13) {
            dispatch("search", searchBox.value);
        }
    };

</script>

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
</style>