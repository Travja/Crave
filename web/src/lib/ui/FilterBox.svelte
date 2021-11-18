<script>
    import {createEventDispatcher, onDestroy, onMount} from "svelte";
    import {fly} from "svelte/transition";

    let dispatch = createEventDispatcher();

    let filterContainer;
    let displayFilters = false;
    const activateFilters = () => displayFilters = !displayFilters;

    let destroy;
    onMount(() => {
        destroy = closeOnClickOutside();
    });

    onDestroy(() => {
        if (destroy) destroy.destroy();
    });

    const closeOnClickOutside = () => {
        const handleClick = (event) => {
            if (displayFilters && !filterContainer.contains(event.target)) {
                displayFilters = false;
                // event.stopImmediatePropagation();
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
    };
</script>

<div class="filter-box">
    <span class="material-icons-round filter-btn" on:click={activateFilters}>filter_alt</span>
    {#if displayFilters}
        <div bind:this={filterContainer} class="filter-container"
             transition:fly="{{ y: -50, duration: 500}}">
            <slot></slot>
            <div class="button apply-btn" on:click={() => dispatch("apply")}>Apply Filters</div>
        </div>
    {/if}
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
        border-radius: 0.6em;
        border: 2px solid var(--fg-color);
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
        margin: 1em auto 0 auto;
    }
</style>