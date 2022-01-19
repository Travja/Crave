<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    //TODO use request params to set current page
    import {page} from "$app/stores";
    import {gateway, title, variables} from "$lib/variables";
    import {onDestroy, onMount} from "svelte";
    import PageItem from "$lib/PageItem.svelte";
    import SearchBar from "$lib/ui/SearchBar.svelte";

    title.set("Items");

    variables.tip = true;
    let gate;
    let items, list = [], fullList = [], error;
    let inProgress = false;

    let pages = 0;
    let currentPage = 0;

    let terms;
    let storeFilter;
    let distanceFilter;
    let sortStrategy;
    let divider;

    const getLocation = (callback) => {
        if (navigator.geolocation)
            navigator.geolocation.getCurrentPosition(pos => callback(pos));
        else
            alert("Geolocation is not supported by this browser.");
    };

    const loadShoppingList = async () => {
        fetch(gateway() + "/auth-service/list")
            .then(res => res.json())
            .then(data => {
                list = [];
                for (let e in data) {
                    list.push(data[e].text);
                    fullList.push(data[e]);
                }
            })
            .catch(e => console.error(e));
    }

    const navigatePage = async i => {
        currentPage = i;
        await getItems();
        divider.scrollIntoView({
            block: 'center',
            behavior: 'smooth'
        });
    };

    const getItems = async () => {
        if (!gate)
            return;

        error = undefined;
        inProgress = true;
        const get = async (queries) => {
            let q = "";
            console.log(queries);
            for (let qu in queries) {
                let param = queries[qu];
                if (qu == "store" && param == "all")
                    continue;
                q += (q.length > 1 ? "&" : "?") + qu + "=" + param;
                console.log(qu + ": " + param);
            }
            let url = gate + '/item-service/items' + q;
            console.log("Fetching items: " + url);

            const res = await fetch(url);
            inProgress = false;
            if (res.ok) {
                let data = await res.json();
                items = data.content;
                pages = data.totalPages;
                currentPage = data.pageable.pageNumber;
                console.log("Items: ", data);
                return;
            }

            error = await res.json();
            // console.error("Could not fetch items.");
            // console.error(error);
            switch (error.status) {
                case 401:
                    error = "User not authenticated.";
                    break;
                case 403:
                    error = "User is not allowed to access this resource";
                    break;
                case 503:
                    error = "Services unavailable";
                    break;
                default:
                    error = "An unknown error occurred.";
            }
        };

        let queries = {};
        if (terms)
            queries.query = terms;
        if (storeFilter)
            queries.store = storeFilter;
        if (sortStrategy)
            queries.sortStrategy = sortStrategy.toUpperCase();

        queries.page = currentPage;
        queries.count = 50;

        if (distanceFilter && distanceFilter > 0) {
            queries.distance = distanceFilter;
            getLocation(pos => {
                console.log(pos);
                queries.lat = pos.coords.latitude;
                queries.lon = pos.coords.longitude;
                get(queries);
            });
        } else {
            await get(queries);
        }
    };

    onMount(async () => {
        // gateway = variables.gateway ? variables.gateway : window.location.origin;
        gate = gateway();
        currentPage = $page.url.searchParams.get("page") ? $page.url.searchParams.get("page") : 0;

        await loadShoppingList();
        await getItems();
    });

    let unsubscribe = variables.jwt.subscribe(() => getItems());
    onDestroy(() => {
        variables.tip = false;
        unsubscribe();
    });

    const search = (e) => {
        terms = e.detail.search;
        storeFilter = e.detail.filters.stores;
        distanceFilter = e.detail.filters.distance;
        sortStrategy = e.detail.filters.sortStrategy;
        getItems();
    };

    const addToList = item => {
        console.log(item);
        list.push(item);
        fullList.push({id: fullList.length + 1, text: item, checked: false});
        let tmp = [...fullList];

        let map = {};
        for (let i = 0; i < tmp.length; i++) {
            map[i] = tmp[i];
        }
        fetch(gateway() + "/auth-service/list", {
            method: "post",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(map)
        })
            .then(res => res.json())
            .then(data => {
                if (!data?.success) {
                    alert("Shopping list could not be saved!");
                } else {
                    list = [...list];
                }
            })
            .catch(err => {
                console.error(err);
            });
    };
</script>
<section>
    <h1>Directory</h1>
    <div id="intro">
        <p>Finding the lowest prices has never been so easy!</p>
    </div>
    <div bind:this={divider} class="divider">
        <hr>
        <div class="sp"/>
        <SearchBar on:search={search}/>
    </div>
    {#if items}
        <div id="items">
            {#each items as item, i}
                {#if item.details}
                    <PageItem {...item} inShoppingList="{list.includes(item.name)}"
                              on:addtolist={() => addToList(item.name)}/>
                    <!--{#if i < items.length - 1}-->
                    <!--    <div class="spacer"/>-->
                    <!--{/if}-->
                {/if}
            {/each}
            {#each Array(8) as i}
                <div class="fix"/>
            {/each}
        </div>
    {:else if inProgress}
        <p class="no-items">Fetching items..</p>
    {:else if error}
        <p class="no-items">Could not fetch items. {error}</p>
    {:else}
        <p class="no-items">No items available.</p>
    {/if}
    <div class="pageSelection">
        {#if pages > 1}
            {#if currentPage > 0}
                <div class="material-icons-round blank-button"
                     on:click={() => navigatePage(currentPage - 1)}>navigate_before
                </div>
            {/if}
            {#each [...Array(5).keys()] as p}
                {#if p + currentPage - 2 >= 0 && p + currentPage - 2 < pages}
                    <div class="pageNum blank-button" class:active={p + currentPage - 2 == currentPage}
                         on:click={() => navigatePage(p + currentPage - 2)}>{p + currentPage - 1}</div>
                {/if}
            {/each}
            {#if pages - 1 > currentPage}
                <div class="material-icons-round blank-button"
                     on:click={() => navigatePage(currentPage + 1)}>navigate_next
                </div>
            {/if}
        {/if}
    </div>
</section>
<style>
    section {
        display: flex;
        flex-direction: column;
        /*justify-content: center;*/
        /*align-items: center;*/
        flex: 1;
        padding: 0.7rem;
    }

    h1 {
        font-size: 2rem;
        margin: 0;
    }

    #intro {
        display: flex;
        align-items: center;
    }

    #items {
        position: relative;
        display: flex;
        flex-wrap: wrap;
        background: var(--bg-color);
    }

    .fix {
        display: flex;
        flex: auto;
        background: transparent;
        padding: 0px 21px;
        flex-grow: 1;
        flex-basis: 15%;
    }

    .fix::after {
        content: "";
        min-width: 200px;
        max-width: 200px;
    }

    .no-items {
        color: #666;
        margin-left: 10px;
    }

    hr {
        width: 30%;
        margin: 0.5em 0;
    }

    .divider {
        display: flex;
        align-items: center;
        margin: 5px 0;
    }

    hr {
        display: inline-block;
    }

    .pageSelection {
        margin: 1em auto 0 auto;
        display: flex;
        flex-direction: row;
    }

    .pageSelection .pageNum {
        margin: 0 0.5em;
    }

    .pageNum.active {
        text-decoration: underline;
        font-weight: bold;
    }

    @media only screen and (min-width: 768px) {
        section {
            padding: 1rem;
        }
    }

</style>