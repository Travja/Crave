<script>
    import {gateway, title, variables} from "$lib/variables";
    import {fly, scale, slide} from "svelte/transition";
    import {elasticInOut, quintOut} from "svelte/easing";
    import {onDestroy, onMount} from "svelte";
    import Thumb from "$lib/ui/Thumb.svelte";
    import FilterBox from "$lib/ui/FilterBox.svelte";
    import {formatter} from "$lib/util";

    title.set("Shopping List");

    const custom = (node, {
        delay = 0,
        duration = 400,
        easing: easing$1 = elasticInOut,
        start = 0,
        opacity = 0,
        enabled = true
    } = {}) => {
        if (!enabled) return;

        const style = getComputedStyle(node);
        const target_opacity = +style.opacity;
        const transform = style.transform === 'none' ? '' : style.transform;
        const sd = 1 - start;
        const od = target_opacity * (1 - opacity);
        return {
            delay,
            duration,
            easing: easing$1,
            css: (_t, u) => `
			position: absolute;
			transform: ${transform} scale(${1 - (sd * u)});
			opacity: ${target_opacity - (od * u)}
		`
        };
    };


    let items = [
        {
            text: "",
            checked: false,
            cost: 0,
            store: "",
            uid: {}
        }
    ];

    let itemContainer, priceContainer, dataList = [];
    let priceStrategy = "cheapest-price";
    let complete = 0;

    let pos1 = 0, pos2 = 0;
    let tracking, activeIndex, targetIndex = -1;
    let active = -1, focused = -1;
    let dList;

    let itmCount = 0;
    $: itmCount = items.length - 1;

    let totalCost = 0, lowestCost = 0;
    let cheapestStore = "Unknown";
    let numStores = 0;
    let lowestStore = -1;

    let unsubscribe = () => {
    };
    onMount(() => {
        unsubscribe = variables.jwt.subscribe(getList);
    });
    onDestroy(unsubscribe);

    $: {
        complete = 0;
        items.forEach(item => {
            if (item.checked)
                complete++;
        });

        if (items.length == 0 ||
            (items.length > 0 && items[items.length - 1].text && items[items.length - 1].text.length > 0)) {
            items.push({
                text: "",
                checked: false,
                cost: 0,
                store: "",
                uid: {}
            });
        }

        if (items.length > 1 && !items[items.length - 2].text) {
            items.pop();
            items[items.length - 1].checked = false;
        }

        if (items.length > 1) {
            totalCost = 0;
            let stores = [];
            items.forEach(item => {
                if (item.lowestDetails) {
                    if (!stores.includes(item.lowestDetails.store.id))
                        stores.push(item.lowestDetails.store.id);
                    totalCost += item.lowestDetails.price;
                }
            });
            numStores = stores.length;
        }
    }

    const getList = () => {
        fetch(`${gateway()}/auth-service/list?detailed=true&priceStrategy=` +
            `${priceStrategy.toUpperCase().replace("-", "_")}`)
            .then(res => res.json())
            .then(data => {
                if (!data.status || data.status == 200) {
                    let list = [];

                    for (let e in data) {
                        list.push(data[e]);
                    }

                    items = list;
                    items.forEach(itm => {
                        itm.uid = {};
                        itm.id = undefined;

                        if (itm.lowestDetails)
                            lowestStore = itm.lowestDetails.store.id;

                        if (itm.lowestDetails?.sales) {
                            let lowestCost = itm.lowestDetails.price;
                            itm.lowestDetails.sales.forEach(sale => {
                                if (sale.newPrice < lowestCost)
                                    lowestCost = sale.newPrice;
                            });

                            itm.lowestDetails.price = lowestCost;
                        }
                    });
                }
            })
            .catch(e => {
                console.error(e);
            });
    };

    const saveList = () => {
        let tmp = [...items];
        if (tmp[tmp.length - 1]?.text == "")
            tmp.pop();

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
                }
            })
            .catch(err => {
                console.error(err);
            });
    };

    const checkNext = (e, index) => {
        if (e.keyCode == 13) {
            if (focused > -1) {
                e.stopPropagation();
                e.preventDefault();
                let focusedVal = dataList[focused];
                items[index].text = focusedVal;
                focused = -1;
            } else {
                let next = document.getElementById("input" + (index + 1));
                if (next) next.focus();
                e.preventDefault();
            }
        }
    };

    const checkPrev = (e, index) => {
        if (e.keyCode == 8 && e.target.innerText == "") {
            let prev = document.getElementById("input" + (index - 1));
            if (prev) {
                prev.focus();
                if (prev.innerText.length > 0) {
                    let sel = window.getSelection();
                    let range = document.createRange();
                    range.setStart(prev.firstChild, prev.innerHTML.length);
                    sel.removeAllRanges();
                    sel.addRange(range);
                }
                e.stopPropagation();
                e.preventDefault();
            }
        }
    };

    const checkDataList = (e) => {
        if (dataList.length > 0) {
            if (e.keyCode == 40) {
                if (focused < dataList.length - 1)
                    focused++;
                e.preventDefault();
            } else if (e.keyCode == 38) {
                if (focused > 0)
                    focused--;
                e.preventDefault();
            }
        }
    };

    const clickDataList = (e, index) => {
        items[index].text = e.target.innerText;
        focused = -1;
        getApplicableItems(e, index);

    };

    const check = index => {
        let itm = items[index];
        itm.checked = !itm.checked;
        let its = [];
        let checked = [];
        items.forEach(item => {
            if (!item.checked && item.text)
                its.push(item);
            else
                checked.push(item);
        });
        items = [...its, ...checked];
        saveList();
    };

    const dragStart = e => {
        e.preventDefault();
        tracking = e.target.parentNode;
        targetIndex = activeIndex = parseInt(tracking.getAttribute("index"));
        pos1 = e.clientY + tracking.getBoundingClientRect().height;
    };

    const dragging = e => {
        if (!tracking) return;
        e.preventDefault();
        pos2 = pos1 - e.clientY;
        pos1 = e.clientY;
        tracking.style.top = (tracking.offsetTop - pos2) + "px";
    };

    const endDrag = e => {
        if (!tracking) return;

        e.preventDefault();
        let itm = items[activeIndex];

        let tmp = [];
        if (targetIndex < 0 || targetIndex == NaN) targetIndex = activeIndex;
        for (let i = 0; i < items.length; i++) {
            if (targetIndex == i)
                tmp.push(itm);

            if (activeIndex == i) continue;

            tmp.push(items[i]);
        }

        tracking.style = undefined;
        items = [...tmp];
        tracking = undefined;
        activeIndex = targetIndex = -1;
        saveList();
    };

    const dragOver = e => {
        if (!tracking || e.target == null) return;
        let target = e.target;
        while (!target.getAttribute("index") && target.parentNode) {
            target = target.parentNode;
        }
        e.preventDefault();
        targetIndex = parseInt(target.getAttribute("index"));
    };

    const getApplicableItems = (e, index) => {
        if (e.keyCode) {
            if (e.keyCode == 27) {
                dataList = [];
                return;
            } else if (e.keyCode == 13) {
                e.stopPropagation();
                e.preventDefault();
            }
        }

        let target = e.target;
        let input = target.innerText;
        // let input = target.value;

        if (input.length == 0) {
            dataList = [];
            return;
        }

        let url = `${gateway()}/item-service/items/search?detailed=true&query=${input}`;
        if (priceStrategy == 'single-price')
            url += `&storeId=${lowestStore}`;

        fetch(url)
            .then(res => res.json())
            .then(data => {
                let dList = [];
                if (data.length >= 1) {
                    items[index].lowestDetails = data[0].lowestDetails;
                } else {
                    items[index].lowestDetails = undefined;
                }
                data.forEach(dat => {
                    dList.push(dat.text);
                });
                dataList = dList;
            })
            .catch(e => {
                console.error(e);
            });
    };

    const del = (index) => {
        items.splice(index, 1);
        items = [...items];
        saveList();
    };

    const applyFilters = () => {
        console.log("applying filters");
        let selected = priceContainer.querySelector("input[name='display']:checked");
        priceStrategy = selected.value;
        console.log(selected);
        saveList();
        getList();
    };


</script>

<div class="container" out:slide>
    <div class="header">
        <div class="count">
            {#key itmCount}
                <span style="display: inline-block" in:fly={{y: -10}}>{itmCount}</span>
            {/key}
            items.
        </div>
        <div class="complete">
            (
            {#key complete}<span style="display: block" in:fly={{y: -10}}>{complete}</span>{/key}/
            {#key itmCount}<span style="display: block" in:fly={{y: -10}}>{itmCount}</span>{/key}
            &nbsp;complete)
        </div>
        <FilterBox on:apply={applyFilters}>
            <div bind:this={priceContainer} class="priceContainer">
                <h4>Price Display</h4>
                <label><input name="display" type="radio" value="single-price"/>Single Store Price</label>
                <label><input checked name="display" type="radio" value="cheapest-price"/>Cheapest Price</label>
            </div>
        </FilterBox>
    </div>
    <div bind:this={itemContainer} class="items"
         on:mouseleave={endDrag}
         on:mousemove={dragging}
         on:mouseup={endDrag}>
        {#each items as itm, i (itm.uid)}
            {#if targetIndex == i}
                <div class="filler row"><span class="material-icons-round drag">keyboard_double_arrow_right</span>
                    <div class="material-icons-round check">
                        check_box_outline_blank
                    </div>
                    <span class="wrapper">
                        <span class="placeholder">{items[activeIndex].text}</span>
                        <input type="text" placeholder="{items[activeIndex].text}"/>
                    </span>
                </div>
            {/if}
            <div class="row"
                 class:active={activeIndex == i}
                 index="{i}"
                 on:mousemove={dragOver}
                 transition:slide>
                <Thumb canDrag="{!!itm.text}" on:dragstart={dragStart}/>
                <div class="checkWrap">
                    {#if itm.text}
                        {#if itm.checked}
                            <div class="material-icons-round check"
                                 on:click={() => check(i)}
                                 out:custom={{duration: 500}}
                                 in:scale={{duration: 750, easing: elasticInOut}}>
                                check_box
                            </div>
                        {:else}
                            <div class="material-icons-round check"
                                 on:click={() => check(i)}
                                 out:custom={{duration: 500}}
                                 in:scale={{duration: 750, easing: elasticInOut}}>
                                check_box_outline_blank
                            </div>
                        {/if}
                    {:else}
                        <div class="material-icons-round add"
                             in:scale={{duration: 750, easing: elasticInOut}}
                             out:custom={{enabled: itm.text, duration: 500, easing: quintOut}}>add
                        </div>
                    {/if}
                </div>

                <div class="wrapper" class:checked={itm.checked}>
                    <!--                    <span class="placeholder">{@html itm.text}</span>-->
                    <!--                    <input type="text" bind:value="{itm.text}" placeholder="New item..."-->
                    <!--                         style="opacity: 0; height: 0;"-->
                    <!--                         on:focus={(e) => document.getElementById(`input${i}`).focus()}-->
                    <div class="textarea" contenteditable="true" wrap="hard"
                         bind:textContent={itm.text}
                         id="input{i}"
                         list="data"
                         on:focus={e => active = i}
                         on:blur={e => focused = active = -1}
                         on:keyup={e => getApplicableItems(e, i)}
                         on:keypress={e => checkNext(e, i)}
                         on:keydown={e => {
                             checkDataList(e);
                             checkPrev(e, i);
                         }}
                    />
                    {#if active == i && dataList.length > 0}
                        <div class="dataListContainer">
                            <div class="dataList" bind:this={dList}>
                                {#each dataList as data, ind}
                                    <div transition:slide
                                         on:mousedown={e => clickDataList(e, i)}
                                         class="dataItem" class:focus={focused == ind}>{data}</div>
                                {/each}
                            </div>
                        </div>
                    {/if}
                    {#if itm.lowestDetails}
                        <div class="details"
                             transition:slide>
                            <div class="predicted-name">{itm.lowestDetails.item.name}</div>
                            <div>
                                {formatter.format(itm.lowestDetails.price)} -
                                {itm.lowestDetails.store.name} -
                                {itm.lowestDetails.store.streetAddress}
                            </div>
                        </div>
                    {:else if itm.text.length > 1 && !itm.lowestDetails && priceStrategy == "single-price"}
                        <div class="details"
                             transition:slide>
                            <div class="predicted-name">Not found at store with other items.</div>
                        </div>
                    {/if}
                </div>
                <div class="material-icons-round close"
                     on:click={e => del(i)}>close
                </div>
            </div>
        {/each}
    </div>

    <div>Cheapest Single-Store Cost: {formatter.format(lowestCost)} - {cheapestStore}</div>
    <div>Lowest Cost: {formatter.format(totalCost)}</div>
    <div>Number of Stores: {numStores}</div>

    <div class="button" on:click={saveList}>Save List</div>
</div>

<style>
    .container {
        min-width: 30%;
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        /*width: 30%;*/
        margin: 0 auto;
        flex-grow: 1;
        max-width: 80%;
    }

    .container > * {
        max-width: 100%;
    }

    .items, .header {
        width: 100%;
    }

    .row {
        position: relative;
        padding: 0.5em;
        background: var(--bg-color);
        border-bottom: 1px solid;
        border-image-source: linear-gradient(to right, rgba(0, 0, 0, 0), var(--disabled-fg-color),
        rgba(0, 0, 0, 0), rgba(0, 0, 0, 0));
        border-image-slice: 1;
        display: flex;
        align-items: center;
        transition: border 1s linear, transform 0.2s ease-in-out;
        max-width: 100%;
    }

    .row.active {
        position: absolute;
        pointer-events: none;
        z-index: 5;
        /*transform: rotate(15deg);*/
    }

    .items > div:last-child {
        /*border-bottom: 1px solid;*/
        border-bottom: none;
    }

    .add, .check {
        user-select: none;
    }

    .checkWrap {
        margin-right: 0.5rem;
        display: flex;
    }

    .check:hover {
        cursor: pointer;
    }

    input, .textarea {
        /*flex: 1;*/
        border: none;
        padding: 0 0 0 0.5rem;
        background: transparent;
        margin: 0;
        font-size: 1rem;
        position: relative;
        word-wrap: anywhere;
        transition: color 0.5s;
    }

    .textarea {
        /*display: block;*/
        /*height: auto;*/
    }

    input::-webkit-calendar-picker-indicator {
        display: none !important;
    }

    input:focus, .textarea:focus {
        outline: none;
    }

    .complete, .header {
        display: flex;
    }

    .count {
        flex-grow: 1;
    }

    .complete {
        margin-left: 0.5rem;
        color: var(--disabled-fg-color)
    }

    .filler {
        min-height: 1em;
        background: var(--secondary-color);
    }

    .placeholder {
        padding-left: 0.5rem;
        /*padding-right: 2rem;*/
        white-space: nowrap;
        font-size: 1em;
        height: 0px;
        overflow: hidden;
    }

    .wrapper {
        position: relative;
        display: flex;
        flex-direction: column;
        border-left: 1px solid black;
        flex: 1;
    }

    .button {
        width: 30%;
        min-width: 200px;
    }

    .row .close {
        opacity: 0;
        transition: opacity 0.2s ease-in-out;
    }

    .row:hover .close {
        opacity: 1;
    }

    .close {
        color: var(--disabled-fg-color);
    }

    .close:hover {
        cursor: pointer;
    }

    .details {
        margin-left: 1rem;
        font-size: 0.8rem;
        color: var(--disabled-fg-color);
    }

    .predicted-name {
        font-size: 0.6rem;
    }

    .dataListContainer {
        height: 0;
        position: relative;
        padding: 0 10px;
        font-size: 0.9rem;
    }

    .dataList {
        position: absolute;
        background: var(--bg-color);
        border: 1px solid black;
        z-index: 50;
    }

    .dataList > div {
        padding: 0.3em;
    }

    .dataItem:hover {
        background: var(--secondary-color);
        cursor: pointer;
    }

    .focus {
        background: var(--secondary-color);
    }

    .checked {
        color: var(--disabled-fg-color);
    }

    input[type="radio"] {
        margin-right: 0.5em;
    }

    .priceContainer {
        display: flex;
        flex-direction: column;
        border-left: 0.2em solid var(--accent-color);
        padding-left: 0.5em;
    }

    .priceContainer h4 {
        margin: 0 0 0.5em;
    }
</style>