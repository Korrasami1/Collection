using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

//https://www.redblobgames.com/pathfinding/a-star/performance.html
//https://www.redblobgames.com/pathfinding/a-star/introduction.html
/// Based on uniform-cost-search/A* from the book
/// Artificial Intelligence: A Modern Approach 3rd Ed by Russell/Norvig

public class Astar_behaviour : ScriptableObject {
    public List<GameObject> currententities;
    public List<GameObject> path;

    public static readonly Vector2[] directions = new Vector2[] {
            new Vector2(1, 0), // to right of tile
            new Vector2(0, -1), // below tile
            new Vector2(-1, 0), // to left of tile
            new Vector2(0, 1), // above tile
        };

    public int square1_x = 0, square1_y = 0, size_x = 10, size_y = 10;

    public bool is_within_grid_boundary(GameObject id_)
    {
        GridMap id = id_.GetComponent<GridMap>();
        return (square1_x <= id.x) && (id.x < size_x) && (square1_y <= id.y) && (id.y < size_y);
    }

    // Everything that isn't a crater is non_blocked and can be moved through by the player
    public bool is_movable(GameObject id_)
    {
        if(id_.tag == "Crater")
        {
            Debug.Log("ran crater is_movable");
            return false;
        }
        return true;
    }

    public int Cost(GameObject a, GameObject b)
    {
        return (int)b.transform.position.sqrMagnitude;
    }

    public IEnumerable<GameObject> Find_Neighbors(GameObject id)
    {
        foreach (Vector2 dir in directions)
        {
            GameObject next_obj = getEntity(id.transform.position.x + dir.x, id.transform.position.y + dir.y);
            if (next_obj != null)
            {
                if (is_within_grid_boundary(next_obj) && is_movable(next_obj))
                {
                     yield return next_obj;
                }
            }
        }
    }

    GameObject getEntity(float x, float y)
    {
        GameObject foundobj = null; //temp value
        Vector3 pos = new Vector3(x, y);
        for (int i = 0; i < currententities.Count; i++)
        {
            if (currententities[i].transform.position == pos && currententities[i].name == "Grid Square(Clone)")
            {
                foundobj = currententities[i];
                break;
            }
        }
        return foundobj;
    }

    /*start of the astar search*/
    public Dictionary<GameObject, GameObject> came_from = new Dictionary<GameObject, GameObject>();
    public Dictionary<GameObject, float> cost = new Dictionary<GameObject, float>();

    private GameObject start;
    private GameObject goal;

    static public float Heuristic_Calculater(GameObject a, GameObject b)
    {
        return Mathf.Abs(a.transform.position.x - b.transform.position.x) + Mathf.Abs(a.transform.position.y - b.transform.position.y);
    }

    public void ASearch(GameObject start, GameObject goal)
    {
        this.start = start;
        this.goal = goal;

        PriorityQueue<GameObject> frontier_list_of_objects = new PriorityQueue<GameObject>();
        frontier_list_of_objects.addElement(start, 0f);
        
        came_from.Add(start, start); //was start, start
        cost.Add(start, 0f);

        while (frontier_list_of_objects.Count > 0f)
        {
            GameObject current = frontier_list_of_objects.getElement();
            //Debug.Log("code ran.....while"+current.transform.position);
            if (current.GetComponent<GridMap>().Equals(goal)) {
                //Debug.Log("..."+current.transform.position+"..."+goal.transform.position);
                came_from.Add(goal, current);
                break;
            } 

            foreach (GameObject neighbor in Find_Neighbors(current))
            {
                float new_containment_Cost = cost[current] + Cost(current, neighbor);

                if (!cost.ContainsKey(neighbor) || new_containment_Cost < cost[neighbor])
                {
                    if (cost.ContainsKey(neighbor)) //remove item that we are replacing their cost values of
                    {
                        cost.Remove(neighbor);
                        came_from.Remove(neighbor);
                        //Debug.Log("code ran2");
                    }

                    cost.Add(neighbor, new_containment_Cost);
                    float priority = new_containment_Cost + Heuristic_Calculater(neighbor, goal);
                    frontier_list_of_objects.addElement(neighbor, priority);
                    came_from.Add(neighbor, current);
                }
            }
        }

    }

    public List<GameObject> FindPath()
    {
        List<GameObject> open_path = new List<GameObject>();
        List<GameObject> closed_path = new List<GameObject>();
        List<GameObject> chosen_path = new List<GameObject>();
        GameObject current_goal = goal;

        while (!current_goal.Equals(start))
        {
            if (!came_from.ContainsKey(current_goal))
            {
                Debug.Log("the (key //goal) is not in the came_from directionary list");
                return new List<GameObject>();
            }
            chosen_path.Add(current_goal);
            current_goal = came_from[current_goal];
        }

        return chosen_path;
    }

    public void clearGoal()
    {
        came_from = new Dictionary<GameObject, GameObject>();
        cost = new Dictionary<GameObject, float>();
    }
}

//https://gist.github.com/keithcollins/307c3335308fea62db2731265ab44c06
public class PriorityQueue<T>
{

    private List<KeyValuePair<T, float>> elements = new List<KeyValuePair<T, float>>();

    public int Count
    {
        get { return elements.Count; }
    }

    public void addElement(T item, float priority)
    {
        elements.Add(new KeyValuePair<T, float>(item, priority));
    }

    //so i can get a return with lowest priority

    public T getElement()
    {
        int bestIndex = 0;

        for (int i = 0; i < elements.Count; i++)
        {
            if (elements[i].Value < elements[bestIndex].Value)
            {
                bestIndex = i;
            }
        }

        T bestItem = elements[bestIndex].Key;
        elements.RemoveAt(bestIndex);
        return bestItem;
    }
}