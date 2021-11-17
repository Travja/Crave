<script>
    import {createEventDispatcher, onDestroy, onMount} from "svelte";

    let dispatch = createEventDispatcher();

    let filterContainer;
    let displayFilters = false;
    const activateFilters = () => displayFilters = !displayFilters;

    $: {
        if (filterContainer) {
            if (displayFilters)
                filterContainer.show();
            else
                filterContainer.close();
        }
    }

    let destroy;
    onMount(() => {
        if (filterContainer) destroy = closeOnClickOutside(filterContainer);
    });

    onDestroy(() => {
        if (destroy) destroy.destroy();
    });

    function closeOnClickOutside(node) {
        const handleClick = (event) => {
            if (displayFilters && !node.contains(event.target)) {
                displayFilters = false;
                event.stopImmediatePropagation();
            }
        };

        document.addEventListener('click', handleClick, {capture: true});
        return {
            destroy() {
                document.removeEventListener(
                    'click',
                    handleClick,
                    {capture: true}
                );
            }
        }
    }
</script>

<div class="filter-box">
    <span class="material-icons-round filter-btn" on:click={activateFilters}>filter_alt</span>
    <dialog bind:this={filterContainer} class="filter-container">
        <slot></slot>
        <div class="button apply-btn" on:click={() => dispatch("apply")}>Apply Filters</div>
    </dialog>
</div>

<style>
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

    .apply-btn {
        display: block;
        margin: 0 auto;
    }
</style>